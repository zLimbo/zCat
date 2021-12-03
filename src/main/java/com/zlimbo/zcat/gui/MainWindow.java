package com.zlimbo.zcat.gui;

import com.zlimbo.zcat.config.ZCatConfig;
import com.zlimbo.zcat.connect.ConnectionLog;
import com.zlimbo.zcat.connect.ConnectionParam;
import com.zlimbo.zcat.connect.SqlConnector;
import com.zlimbo.zcat.chain.ChainConnect;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainWindow extends VBox {

    private static final Logger logger = LoggerFactory.getLogger(MainWindow.class);

    private int queryTabId = 1;

    private Map<String, String> languageMap = null;

    private int fontSize = 18;

    private Map<String, Table> tableMap = new HashMap<>();

    private final Map<String, ChainConnect> chainControlMap = new HashMap<>();

    private final Map<String, SqlConnector> sqlConnectorMap = new HashMap<>();

    private SqlConnector currentSqlConnector = null;

    private final TreeItem<String> treeItemRoot = new TreeItem<>("root");
    private TreeItem<String> currentTreeItem = null;
    private Button newQueryButton = null;
    private TabPane showTabPane = null;
    private final ToolBar bottomBar;
    private Label msgLabel = new Label();

    public MainWindow() {
        setStyle("-fx-font: " + fontSize + " arial; -fx-font-family: 'Microsoft YaHei UI';");
//        languageMap = ZCatConfig.LANGUAGE_ENGLISH_MAP;
        languageMap = ZCatConfig.LANGUAGE_CHINESE_MAP;
        MenuBar menuBar = buildMenuBar();
        ToolBar toolBar = buildToolBar();
        SplitPane splitPane = buildSplitPane();
        bottomBar = new ToolBar();
        getChildren().addAll(
                menuBar,
                toolBar,
                splitPane,
                bottomBar
        );
        setVgrow(splitPane, Priority.ALWAYS);
        // 没有数据库不显示 query
        newQueryButton.setDisable(true);
        loadConnectionTree();
    }

    private void loadConnectionTree() {
        List<ConnectionParam> connectionParams = ConnectionLog.getConnectionParams();
        for (ConnectionParam connectionParam : connectionParams) {
            SqlConnector sqlConnector = new SqlConnector(connectionParam);
            addSqlConnectorAndTreeItem(sqlConnector);
        }
    }

    private TreeItem<String> addSqlConnectorAndTreeItem(SqlConnector sqlConnector) {
        ConnectionParam connectionParam = sqlConnector.getConnectionParam();
        String connectionName = connectionParam.getDatabase() + " [" +
                connectionParam.getHost() + ":" + connectionParam.getPort() + "]";
        sqlConnectorMap.put(connectionName, sqlConnector);
        // 若已存在，则直接返回
        if (sqlConnectorMap.containsKey(connectionName)) {
            for (TreeItem<String> treeItem : treeItemRoot.getChildren()) {
                if (connectionName.equals(treeItem.getValue())) {
                    return treeItem;
                }
            }
        }
        TreeItem<String> connectionParamTreeItem = new TreeItem<>(connectionName,
                new ImageView(new Image(getClass().getResourceAsStream("/image/database.png"))));
        treeItemRoot.getChildren().add(connectionParamTreeItem);
        ConnectionLog.addConnectionParam(connectionParam);
        return connectionParamTreeItem;
    }

    private void removeConnectionTreeItem(TreeItem treeItem) {
        ConnectionLog.removeConnectionParam(sqlConnectorMap.get(treeItem.getValue()).getConnectionParam());
        treeItemRoot.getChildren().remove(treeItem);
        sqlConnectorMap.remove(treeItem);
    }

    private MenuBar buildMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(true);
        Menu fileMenu = new Menu(languageMap.get("File"));
        Menu editMenu = new Menu(languageMap.get("Edit"));
        Menu optionMenu = new Menu(languageMap.get("Option"));
        Menu helpMenu = new Menu(languageMap.get("Help"));

        ToggleGroup toggleGroup = new ToggleGroup();

        menuBar.getMenus().addAll(
                fileMenu,
                editMenu,
                optionMenu,
                helpMenu
        );
        return menuBar;
    }


    private ToolBar buildToolBar() {
        ToolBar toolBar = new ToolBar();
        Button connectDataBaseButton = new Button(languageMap.get("Connect Database"),
                new ImageView(new Image(getClass().getResourceAsStream("/image/connection.png"))));
        Button connectCitaButton = new Button(languageMap.get("Connect CITA"),
                new ImageView(new Image(getClass().getResourceAsStream("/image/cita.png"))));

        newQueryButton = new Button(languageMap.get("New Query"),
                new ImageView(new Image(getClass().getResourceAsStream("/image/query.png"))));

        toolBar.getItems().addAll(
                connectDataBaseButton,
                connectCitaButton,
                newQueryButton
        );

        connectDataBaseButton.setOnAction(
                event -> newConnection(languageMap.get("MySQL New Connect"), "localhost", "3306", "", ""));
        connectCitaButton.setOnAction(event -> connectionCita());
        newQueryButton.setOnAction(event -> newQuery());

        return toolBar;
    }


    private SplitPane buildSplitPane() {
        SplitPane splitPane = new SplitPane();
        TreeView<String> treeView = new TreeView<>();
        showTabPane = new TabPane();
        splitPane.getItems().addAll(
                treeView,
                showTabPane
        );
        splitPane.setDividerPosition(0, 0.25);
        treeViewConfig(treeView);
        return splitPane;
    }

    private void treeViewConfig(TreeView<String> treeView) {
        treeView.setRoot(treeItemRoot);
        treeView.setShowRoot(false);

        treeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                logger.debug("[handle] start\n");

                if (event.getClickCount() == 2) {
                    TreeItem<String> treeItem = treeView.getSelectionModel().getSelectedItem();
                    String name = treeItem.getValue();
                    logger.debug("item name: " + name);

                    if (sqlConnectorMap.containsKey(name)) {
                        SqlConnector sqlConnector = sqlConnectorMap.get(name);
                        closeCurrentConnection();
//                        if (sqlConnector == currentSqlConnector) {
//                            return;
//                        }
                        if (sqlConnector.openConnect()) {
//                            closeCurrentConnection();
                            currentSqlConnector = sqlConnector;
                            currentTreeItem = treeItem;
                            showDatabaseTableTree();
                        } else {
                            ConnectionParam connectionParam = sqlConnector.getConnectionParam();
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle(languageMap.get("Connect Error"));
                            alert.setHeaderText(languageMap.get("Incorrect connection!"));
                            alert.showAndWait();

                            newConnection(
                                    languageMap.get("Please re-enter connection parameters"),
                                    connectionParam.getHost(),
                                    connectionParam.getPort(),
                                    connectionParam.getDatabase(),
                                    connectionParam.getUser());
                        }
                    } else {
                        String tableName = treeItem.getValue();
                        showTable(tableName);
                    }
                }

                logger.debug("[handle] end");
            }
        });
    }


    /**
     * 为Tab添加右键菜单
     *
     * @param tabName
     * @param tab
     */
    private void tabAddContextMenu(String tabName, Tab tab) {
        logger.debug("[tabAddContextMenu] start [tabName = " + tabName + "]");
        ContextMenu contextMenu = new ContextMenu();
        MenuItem closeMenuItem = new MenuItem(languageMap.get("Close"));
        closeMenuItem.setOnAction(event -> closeTab(tabName, tab));
        contextMenu.getItems().add(closeMenuItem);
        tab.setContextMenu(contextMenu);
        logger.debug("[tabAddContextMenu] end");
    }


    private void switchTab(String tabName) {
        Table table = tableMap.get(tabName);
        showTabPane.getSelectionModel().select(table.getTab());
        msgLabel.setText(table.getMsg());
    }

    /**
     * 添加一个 Tab 的相应操作
     *
     * @param tabName
     * @param tab
     */
    private void addTab(String tabName, Tab tab) {
        logger.debug("[addTab] start [tabName = " + tabName + "]");
        showTabPane.getTabs().add(tab);
        Table table = new Table(tabName, tab);
        tableMap.put(tabName, table);
        switchTab(tabName);
        tab.setOnSelectionChanged(event -> {
            msgLabel.setText(table.getMsg());
        });
        logger.debug("[addTab] end");
    }


    /**
     * 删除一个 Tab 的相应操作
     *
     * @param tabName
     * @param tab
     */
    private void closeTab(String tabName, Tab tab) {
        logger.debug("[closeTab] start [tabName = " + tabName + "]");
        showTabPane.getTabs().remove(tab);
        tableMap.remove(tabName);
        logger.debug("[closeTab] end");
    }


    /**
     * 增加一条数据项
     *
     * @param tableName
     */
    private void addRecord(String tableName) {
        logger.debug("[addRecord] start");

        List<String> columnNames = currentSqlConnector.getColumns(tableName);
        List<TextField> textFields = new ArrayList<>();

        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle(languageMap.get("Add Record"));
        dialog.setHeaderText(null);

        ButtonType submitButtonType = new ButtonType(languageMap.get("Submit"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(languageMap.get("Cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, cancelButtonType);
        Button submitButton = (Button) dialog.getDialogPane().lookupButton(submitButtonType);
        submitButton.setDisable(true);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 150, 10, 10));
        for (int i = 0; i < columnNames.size(); ++i) {
            String columnName = columnNames.get(i);
            Label label = new Label(columnName);
            TextField textField = new TextField();
            textField.setPromptText(columnName);
            gridPane.add(label, 0, i);
            gridPane.add(textField, 1, i);
            textFields.add(textField);
        }
        // 监听，输入不得为空，否则提交按钮为灰色
        for (TextField textField : textFields) {
            textField.textProperty().addListener((observable) -> {
                for (TextField textField1 : textFields) {
                    if (textField1.getText().trim().isEmpty()) {
                        submitButton.setDisable(true);
                        return;
                    }
                }
                submitButton.setDisable(false);
            });
        }

        dialog.getDialogPane().setContent(gridPane);
        // 点击提交，插入数据
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                StringBuilder stringBuilder = new StringBuilder("INSERT INTO " + tableName + " VALUES(");
                for (int i = 0; i < textFields.size(); ++i) {
                    TextField textField = textFields.get(i);
                    stringBuilder.append("'" + textField.getText() + "'");
                    if (i != textFields.size() - 1) {
                        stringBuilder.append(",");
                    }
                }
                stringBuilder.append((");"));
                String sql = stringBuilder.toString();
                currentSqlConnector.sqlUpdate(sql);
                showTable(tableName);   // 更新表显示
            }
            return null;
        });

        dialog.showAndWait();

        logger.debug("[addRecord] end");
    }


    /**
     * 分页显示单个数据的详细情况
     *
     * @param tableView
     * @param pagination
     * @param columns
     * @param records
     */
    private void showPagination(TableView tableView, Pagination pagination, List<String> columns, List<List<String>> records) {
        logger.debug("[showQuerySingle] start");

        TableView singleTableView = new TableView();
        singleTableView.setPlaceholder(
                new ImageView(new Image(getClass().getResourceAsStream("/image/tableEmpty.png"))));
        pagination.setPageCount(records.size());

        if (!records.isEmpty()) {
            TableColumn<List<StringProperty>, String> keyColumn = new TableColumn<>(languageMap.get("Attribute"));
            TableColumn<List<StringProperty>, String> valueColumn = new TableColumn<>(languageMap.get("Value"));
            keyColumn.setCellValueFactory(data -> data.getValue().get(0));
            valueColumn.setCellValueFactory(data -> data.getValue().get(1));
            singleTableView.getColumns().addAll(keyColumn, valueColumn);

            ObservableList<List<StringProperty>> data = FXCollections.observableArrayList();
            List<StringProperty> valuePropertyList = new ArrayList<>();
            for (int i = 0; i < columns.size(); ++i) {
                List<StringProperty> row = new ArrayList<>();
                StringProperty valueProperty = new SimpleStringProperty(records.get(0).get(i));
                valuePropertyList.add(valueProperty);
                row.add(0, new SimpleStringProperty(columns.get(i)));
                row.add(1, valueProperty);
                data.add(row);
            }
            singleTableView.setItems(data);

            pagination.setPageFactory(pageIndex -> {
                for (int i = 0; i < columns.size(); ++i) {
                    valuePropertyList.get(i).setValue(records.get(pageIndex).get(i));
                }
                return singleTableView;
            });

            tableView.getSelectionModel().selectedIndexProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        pagination.setCurrentPageIndex(newValue.intValue());
                    }
            );
        }

        logger.debug("[showQuerySingle] end");
    }


    /**
     * 执行sql语句并将其显示在相应的 TableView 中，同时显示结果信息
     *
     * @param sql
     * @param tableSplitPane
     * @param messageTextArea
     * @return
     */
    private int executeSqlAndShowTableView(String sql, SplitPane tableSplitPane, TextArea messageTextArea) {
        logger.debug("[executeSqlAndShowTableView] start");
        logger.debug("sql: " + sql);
        int returnFlag = 0;
        tableSplitPane.getItems().clear();
        SqlConnector.SqlQueryResult sqlQueryResult = null;
        String oneLineSql = sql.replace("\n", " ");
        String sqlUpCase = sql.toUpperCase().trim();

//        if (sqlUpCase.contains("SET BLOCKCHAIN DATASOURCE")) {
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            messageTextArea.setStyle("-fx-text-fill:#00ff00;");
//            messageTextArea.setText(sql.trim() + "\n> OK" + "\n> 0.5s");
//            return 4;
//        }

        if (sqlUpCase.isEmpty()) {
            sqlQueryResult = new SqlConnector.SqlQueryResult();
            sqlQueryResult.setErrorMessage(languageMap.get("SQL statement cannot be empty!"));
        } else if (sqlUpCase.startsWith("CREATE")) {
            logger.debug("CREATE");
//            huawei();
            sqlQueryResult = currentSqlConnector.sqlCreateTable(oneLineSql);
            long spendTime = sqlQueryResult.getSpendTime();
            returnFlag = 1;
            if (sqlQueryResult.getErrorMessage() == null) {
                showDatabaseTableTree(); // 刷新数据库
                Matcher matcher = Pattern.compile("^\\s*\\w+\\s+\\w+\\s+(\\w+)").matcher(sql);  // 正则获取表名
                if (matcher.find()) {
                    String tableName = matcher.group(1);
                    sqlQueryResult = currentSqlConnector.sqlQuery("DESCRIBE " + tableName);
                    sqlQueryResult.setSpendTime(spendTime);
                    returnFlag = 3;
                }
            }
        } else if (sqlUpCase.startsWith("INSERT") ||
                sqlUpCase.startsWith("DELETE") ||
                sqlUpCase.startsWith("ALTER") ||
                sqlUpCase.startsWith("DROP") ||
                sqlUpCase.startsWith("UPDATE")) {
            logger.debug("UPDATE");
            sqlQueryResult = currentSqlConnector.sqlUpdate(oneLineSql);
            if (sqlUpCase.startsWith("DROP") && sqlQueryResult.getErrorMessage() == null) {
                showDatabaseTableTree();
            }
            returnFlag = 2;
        } else if (sqlUpCase.contains("USING STATE")) { // 暂时未考虑一个空格以外的情况
            logger.debug("USING STATE");
            sqlQueryResult = currentSqlConnector.sqlQueryForState(oneLineSql);
            returnFlag = 3;
        } else {
            logger.debug("SELECT");
            sqlQueryResult = currentSqlConnector.sqlQuery(oneLineSql);
            returnFlag = 3;
        }
        String errorMessage = sqlQueryResult.getErrorMessage();
        List<String> columns = sqlQueryResult.getColumns();
        List<List<String>> records = sqlQueryResult.getRecords();
        long spendTime = sqlQueryResult.getSpendTime();
        if (errorMessage == null) {
            TableView tableView = new TableView();
            tableView.setPlaceholder(
                    new ImageView(new Image(getClass().getResourceAsStream("/image/tableEmpty.png"))));
            tableView.setTableMenuButtonVisible(true);
            Pagination pagination = new Pagination();
            pagination.setPageCount(1);
            tableSplitPane.getItems().addAll(tableView, pagination);
            tableSplitPane.setOrientation(Orientation.HORIZONTAL);
            tableSplitPane.setDividerPosition(0, 0.6);

            for (int i = 0; i < columns.size(); ++i) {
                TableColumn<List<StringProperty>, String> tableColumn = new TableColumn<>(columns.get(i));
                int finalI = i;
                tableColumn.setCellValueFactory(data -> data.getValue().get(finalI));
                tableView.getColumns().add(tableColumn);
            }
            ObservableList<List<StringProperty>> data = FXCollections.observableArrayList();
            for (List<String> record : records) {
                List<StringProperty> row = new ArrayList<>();
                for (int i = 0; i < record.size(); ++i) {
                    row.add(i, new SimpleStringProperty(record.get(i)));
                }
                data.add(row);
            }
            tableView.setItems(data);
            showPagination(tableView, pagination, columns, records);

            if (messageTextArea != null) {
                messageTextArea.setStyle("-fx-text-fill:#00ff00;");
                messageTextArea.setText(sql.trim() + "\n> OK" + "\n> " +
                        languageMap.get("Time") + ": " + (spendTime / 1000.0) + "s");
            }
        } else {
            returnFlag = 0;
            if (messageTextArea != null) {
                messageTextArea.setStyle("-fx-text-fill:#ff0000;");
                messageTextArea.setText(sql.trim() + "\n> Error: " + errorMessage + "\n> "
                        + languageMap.get("Time") + ": " + (spendTime / 1000.0) + "s");
            }
        }
        logger.debug("[executeSqlAndShowTableView] end");
        return returnFlag;
    }


    /**
     * 创建新的查询Tab
     */
    public void newQuery() {
        logger.debug("[newQuery] start");

        String queryTabName = languageMap.get("Query") + "[" + (queryTabId++) + "]";
        Tab queryTab = new Tab(queryTabName);
        queryTab.setGraphic(
                new ImageView(new Image(getClass().getResourceAsStream("/image/query2.png"))));
        addTab(queryTabName, queryTab);
        tabAddContextMenu(queryTabName, queryTab);
        queryTab.setOnClosed(event -> closeTab(queryTabName, queryTab));
        BorderPane borderPane = new BorderPane();
        queryTab.setContent(borderPane);

        ToolBar toolBar = new ToolBar();
        borderPane.setTop(toolBar);
        Button closeButton = new Button(languageMap.get("Close"),
                new ImageView(new Image(getClass().getResourceAsStream("/image/close.png"))));
//        Button saveButton = new Button("Save",
//                new ImageView(new Image(getClass().getResourceAsStream("/image/save.png"))));
        Button runButton = new Button(languageMap.get("Run"),
                new ImageView(new Image(getClass().getResourceAsStream("/image/run.png"))));
        //toolBar.getItems().addAll(closeButton, saveButton, runButton);

        toolBar.getItems().addAll(closeButton, runButton);

        if (ZCatConfig.FOR_TEST) {
            toolBarAddTest(toolBar, runButton, queryTab);
        }

        SplitPane splitPane = new SplitPane();
        borderPane.setCenter(splitPane);

        TextArea textArea = new TextArea();
        SplitPane tableSplitPane = new SplitPane();
        TextArea messageTextArea = new TextArea();
        messageTextArea.setEditable(false);
        splitPane.getItems().add(textArea);
        splitPane.setOrientation(Orientation.VERTICAL);


        closeButton.setOnAction(event -> {
            closeTab(queryTabName, queryTab);
        });

        runButton.setOnAction(event -> {
            splitPane.getItems().clear();
            int flag = executeSqlAndShowTableView(textArea.getText(), tableSplitPane, messageTextArea);
            logger.debug("flag: " + flag);
            if (flag == 3) {
                splitPane.getItems().addAll(textArea, tableSplitPane, messageTextArea);
                splitPane.setDividerPosition(0, 0.2);
                splitPane.setDividerPosition(1, 0.85);
            } else {
                splitPane.getItems().addAll(textArea, messageTextArea);
                splitPane.setDividerPosition(0, 0.5);
            }
        });

        logger.debug("[newQuery] end");
    }


    /**
     * 显示一个数据库表
     *
     * @param tableName
     */
    public void showTable(String tableName) {
        logger.debug("[showTable] start");
        Tab tableTab;
        SplitPane tableSplitPane;
        String tabName = tableName;
        if (tableMap.containsKey(tabName)) {
            tableTab = tableMap.get(tabName).getTab();
            tableSplitPane = (SplitPane) ((BorderPane) tableTab.getContent()).getCenter();
        } else {
            tableTab = new Tab(tableName);
            tableTab.setGraphic(
                    new ImageView(new Image(getClass().getResourceAsStream("/image/table.png")))
            );
            addTab(tabName, tableTab);
            tabAddContextMenu(tableName, tableTab);
            tableTab.setOnClosed(event1 -> closeTab(tableName, tableTab));
            BorderPane borderPane = new BorderPane();
            ToolBar toolBar = new ToolBar();
            Button closeButton = new Button(languageMap.get("Close"),
                    new ImageView(new Image(getClass().getResourceAsStream("/image/close.png"))));
            Button addButton = new Button(languageMap.get("Add"),
                    new ImageView(new Image(getClass().getResourceAsStream("/image/add.png"))));
            toolBar.getItems().addAll(closeButton, addButton);
            borderPane.setTop(toolBar);
            tableSplitPane = new SplitPane();
            borderPane.setCenter(tableSplitPane);
            tableTab.setContent(borderPane);

            closeButton.setOnAction(event -> {
                closeTab(tableName, tableTab);
            });
            addButton.setOnAction(event -> addRecord(tableName));
        }

        // 分页
//        splitPage(tableName);

        Table table = tableMap.get(tabName);
        int pageRecordNum = table.getPageRecordNum();
        int pageIndex = table.getPageIndex();
        int tableCount = currentSqlConnector.selectCount(tableName);
        int pageCount = (tableCount + pageRecordNum - 1) / pageRecordNum;
        if (pageCount == 0) {
            pageCount = 1;
        }
        Pagination pagination = new Pagination();
        pagination.setPageCount(pageCount);

        pagination.setPageFactory(pageIndex1 -> {
            table.setPageIndex(pageIndex1);
            String sql = "SELECT * FROM " + tableName + " LIMIT " + pageIndex1 * pageRecordNum + ", " + pageRecordNum;
            logger.debug("page: " + pageIndex1 + ", sql: " + sql);
            SqlConnector.SqlQueryResult sqlQueryResult = currentSqlConnector.sqlQuery(sql);
            List<String> columns = sqlQueryResult.getColumns();
            List<List<String>> records = sqlQueryResult.getRecords();

            TableView tableView = new TableView();
            tableView.setPlaceholder(
                    new ImageView(new Image(getClass().getResourceAsStream("/image/tableEmpty.png"))));
            tableView.setTableMenuButtonVisible(true);
            for (int i = 0; i < columns.size(); ++i) {
                TableColumn<List<StringProperty>, String> tableColumn = new TableColumn<>(columns.get(i));
                int finalI = i;
                tableColumn.setCellValueFactory(data -> data.getValue().get(finalI));
                tableView.getColumns().add(tableColumn);
            }
            ObservableList<List<StringProperty>> data = FXCollections.observableArrayList();
            for (List<String> record : records) {
                List<StringProperty> row = new ArrayList<>();
                for (int i = 0; i < record.size(); ++i) {
                    row.add(i, new SimpleStringProperty(record.get(i)));
                }
                data.add(row);
            }
            tableView.setItems(data);

            String msg = "\t\t" + sql;
            table.setMsg(msg);
            msgLabel.setText(msg);
            tableView.getSelectionModel().selectedIndexProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        String msg1 = "\t\t" + sql + "\t\t第" + newValue.intValue() +
                                "条记录（共" + records.size() + "条）于第" + (pageIndex1 + 1) + "页";
                        msgLabel.setText(msg1);
                        table.setMsg(msg1);
                    }
            );
            return tableView;
        });

//        String sql = "SELECT * FROM " + tableName + " LIMIT ";
//        String sql = "SELECT * FROM " + tableName;
//        executeSqlAndShowTableView(sql, tableSplitPane, null);


        pagination.setCurrentPageIndex(pageIndex);
        tableSplitPane.getItems().clear();
        tableSplitPane.getItems().addAll(pagination);
        switchTab(tabName);
//        showTabPane.getSelectionModel().select(tableTab);

        logger.debug("[showTable] end");
    }


//    private void splitPage(String tableName) {
//
//        int count = currentSqlConnector.selectCount(tableName);
//        System.out.println("count: " + count);
//        Table table = tableMap.get(tableName);
//        int pageRecordNum = table.getPageRecordNum();
//        int currentPageId = table.getCurrentPageId();
//
//        ToolBar bottomToolBar = new ToolBar();
//        Button leftmostButton = new Button("|<");
//        Button leftButton = new Button("<");
//        Button rightmostButton = new Button(">|");
//        Button rightButton = new Button(">");
//        TextField pageIdTextField = new TextField();
//        int length = String.valueOf((count + pageRecordNum - 1) / pageRecordNum).length();
//        pageIdTextField.setPrefColumnCount(length);
//        pageIdTextField.setText(String.valueOf(currentPageId));
//        bottomToolBar.getItems().addAll(leftmostButton, leftButton, pageIdTextField, rightButton, rightmostButton);
//
//        BorderPane borderPane = (BorderPane) table.getTab().getContent();
//        borderPane.setBottom(bottomToolBar);
//    }


    /**
     * 显示数据库树形菜单
     */
    private void showDatabaseTableTree() {
        logger.debug("[showDatabaseTableTree] start");

        if (currentSqlConnector == null) {
            return;
        }

        currentTreeItem.getChildren().clear();
        bottomBar.getItems().clear();

        List<String> tables = currentSqlConnector.sqlShowTables();
        for (String table : tables) {
            TreeItem<String> tableItem = new TreeItem<>(table,
                    new ImageView(new Image(getClass().getResourceAsStream("/image/table.png"))));
            currentTreeItem.getChildren().add(tableItem);
//            // TODO 待删除
//            break;
        }
        currentTreeItem.setExpanded(true);

        bottomBar.getItems().clear();
        Label dbInfoLabel = new Label("connection: [" + currentSqlConnector.getUser() + "@" +
                currentSqlConnector.getHost() + ":" +
                currentSqlConnector.getPort() + ":" +
                currentSqlConnector.getDatabase() + "]");
        bottomBar.getItems().add(dbInfoLabel);
        bottomBar.getItems().add(msgLabel);

        newQueryButton.setDisable(false);
        logger.debug("[showDatabaseTableTree] end");
    }


    public void newConnection(String title, String host, String port, String database, String user) {
        logger.debug("[connectDatabase] start");
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        ButtonType connectButtonType = new ButtonType(languageMap.get("Connect"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(languageMap.get("Cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(connectButtonType, cancelButtonType);
        Button connectButton = (Button) dialog.getDialogPane().lookupButton(connectButtonType);
        connectButton.setDisable(true);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 150, 10, 30));

        List<TextField> textFields = new ArrayList<>();

        Label hostLabel = new Label("Host: ");
        TextField hostTextField = new TextField(host);
        gridPane.add(hostLabel, 0, 0);
        gridPane.add(hostTextField, 1, 0);
        textFields.add(hostTextField);

        Label portLabel = new Label("Port: ");
        TextField portTextField = new TextField(port);
        gridPane.add(portLabel, 0, 1);
        gridPane.add(portTextField, 1, 1);
        textFields.add(portTextField);

        Label databaseLabel = new Label("Database: ");
        TextField databaseTextField = new TextField(database);
        gridPane.add(databaseLabel, 0, 2);
        gridPane.add(databaseTextField, 1, 2);
        textFields.add(databaseTextField);

        Label userLabel = new Label("User: ");
        TextField userTextField = new TextField(user);
        gridPane.add(userLabel, 0, 3);
        gridPane.add(userTextField, 1, 3);
        textFields.add(userTextField);

        Label passwordLabel = new Label("Password: ");
        PasswordField passwordField = new PasswordField();
        gridPane.add(passwordLabel, 0, 4);
        gridPane.add(passwordField, 1, 4);
        textFields.add(passwordField);

        for (TextField textField : textFields) {
            textField.textProperty().addListener((observable) -> {
                for (TextField textField1 : textFields) {
                    if (textField1.getText().trim().isEmpty()) {
                        connectButton.setDisable(true);
                        return;
                    }
                }
                connectButton.setDisable(false);
            });
        }

        dialog.getDialogPane().setContent(gridPane);
        // 提交数据
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == connectButtonType) {
                ConnectionParam connectionParam = new ConnectionParam(
                        hostTextField.getText(),
                        portTextField.getText(),
                        databaseTextField.getText(),
                        userTextField.getText(),
                        passwordField.getText());
                SqlConnector sqlConnector = new SqlConnector(connectionParam);

                if (sqlConnector.openConnect()) {
                    closeCurrentConnection();
                    TreeItem<String> treeItem = addSqlConnectorAndTreeItem(sqlConnector);
                    currentSqlConnector = sqlConnector;
                    currentTreeItem = treeItem;
                    showDatabaseTableTree();
                } else {
                    //logger.debug("database connect fail");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(languageMap.get("Connect Error"));
                    alert.setHeaderText(languageMap.get("Incorrect connection!"));
                    alert.showAndWait();
                }
            }
            return null;
        });

        dialog.showAndWait();

        logger.debug("[connectDatabase] end");
    }

    private void closeCurrentConnection() {
        if (currentSqlConnector == null) {
            return;
        }
        currentSqlConnector.closeConnect();
        currentTreeItem.getChildren().clear();
        showTabPane.getTabs().clear();
        tableMap.clear();
        queryTabId = 1;
        newQueryButton.setDisable(false);
    }

    public void showCita(String citaUrl) {
        logger.debug("[showCita] start");
        String tabName = citaUrl;
        if (tableMap.containsKey(tabName)) {
            switchTab(tabName);
//            showTabPane.getSelectionModel().select(tableMap.get(citaUrl).getTab());
            return;
        }

        ChainConnect chainConnect = chainControlMap.get(citaUrl);
        chainConnect.updateStart();
        Tab citaTab = new Tab(citaUrl + " @CITA");

        addTab(citaUrl, citaTab);
        tabAddContextMenu(citaUrl, citaTab);
        citaTab.setOnClosed(event -> closeTab(citaUrl, citaTab));

        citaTab.setGraphic(
                new ImageView(new Image(getClass().getResourceAsStream("/image/cita2.png")))
        );
        BorderPane borderPane = new BorderPane();

        ToolBar toolBar = new ToolBar();
        Button closeButton = new Button(languageMap.get("Close"),
                new ImageView(new Image(getClass().getResourceAsStream("/image/close.png"))));
        toolBar.getItems().addAll(closeButton);
        borderPane.setTop(toolBar);
        TableView tableView = new TableView();
        borderPane.setCenter(tableView);
        citaTab.setContent(borderPane);

        closeButton.setOnAction(event -> {
            chainConnect.updateStop();
            chainControlMap.remove(citaUrl);
            closeTab(citaUrl, citaTab);
        });

        TableColumn<List<StringProperty>, String> keyColumn = new TableColumn<>("key");
        TableColumn<List<StringProperty>, String> valueColumn = new TableColumn<>("value");
        keyColumn.setCellValueFactory(data -> data.getValue().get(0));
        valueColumn.setCellValueFactory(data -> data.getValue().get(1));
        tableView.getColumns().addAll(keyColumn, valueColumn);

        ObservableList<List<StringProperty>> data = FXCollections.observableArrayList();
        List<List<StringProperty>> bcInfo = chainConnect.getBcInfo();

        for (List<StringProperty> list : bcInfo) {
            List<StringProperty> row = new ArrayList<>();
            row.add(0, list.get(0));
            row.add(1, list.get(1));
            data.add(row);
        }
        tableView.setItems(data);
        logger.debug("[showCita] end");
    }


    /**
     * 连接 cita
     */
    public void connectionCita() {
        logger.debug("[connectionCita] start");
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        //dialog.setWidth(200);
        dialog.setTitle(languageMap.get("CITA New Connect"));
        dialog.setHeaderText(null);

        ButtonType connectButtonType = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(connectButtonType, cancelButtonType);
        Button connectButton = (Button) dialog.getDialogPane().lookupButton(connectButtonType);
        connectButton.setDisable(true);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(30, 60, 10, 20));

        Label citaUrlLabel = new Label(languageMap.get("CITA URL: "));
        TextField citaUrlTextField = new TextField();
        citaUrlTextField.setPrefWidth(300);
        citaUrlTextField.textProperty().addListener(observable -> {
            connectButton.setDisable(citaUrlTextField.getText().trim().isEmpty());
        });
        gridPane.add(citaUrlLabel, 0, 0);
        gridPane.add(citaUrlTextField, 1, 0);

        dialog.getDialogPane().setContent(gridPane);
        // 提交数据
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == connectButtonType) {
                String citaUrl = citaUrlTextField.getText().trim();
                if (!chainControlMap.containsKey(citaUrl)) {
                    ChainConnect chainConnect = new ChainConnect(citaUrl);
                    if (chainConnect.isConnectSuccess()) {
                        chainControlMap.put(citaUrl, chainConnect);
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error Connection");
                        alert.setHeaderText("Invalid connection!");
                        alert.showAndWait();
                        return null;
                    }
                }
                showCita(citaUrl);
            }
            return null;
        });

        dialog.showAndWait();

        logger.debug("[connectDatabase] end");
    }


    private void toolBarAddTest(ToolBar toolBar, Button runButton, Tab queryTab) {
        TextField testTextField = new TextField();
        testTextField.setPromptText("运行次数");
        Button testButton = new Button("运行测试");
        toolBar.getItems().addAll(testTextField, testButton);

        testButton.setOnAction(event -> {
            int frequency = 0;
            try {
                frequency = Integer.parseInt(testTextField.getText());
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return;
            }
            ((SplitPane) ((BorderPane) queryTab.getContent()).getCenter()).getItems().clear();
//                System.gc();
            long start = System.currentTimeMillis();
            currentSqlConnector.setTestExecuteNum(frequency);
            runButton.fire();
            currentSqlConnector.setTestExecuteNum(1);
            double spendTime = (System.currentTimeMillis() - start) / 1000d;
            System.out.println("循环了 " + frequency + " 次， 总耗时为 " + spendTime + " s");
        });
    }
}

//    static int x = 1;

//    void huawei() {
//
//        String sql1 = "CREATE TABLE invoice1 (\n" +
//                "\tSYSTEM_ID VARCHAR ( 255 ),\n" +
//                "\tREQUEST_SN VARCHAR ( 255 ),\n" +
//                "\tINVOKE_TIME datetime,\n" +
//                "\tBUSINESS_ID VARCHAR ( 255 ),\n" +
//                "\tCALLBACK_URL VARCHAR ( 255 ),\n" +
//                "\tKEY_ID VARCHAR ( 255 ),\n" +
//                "\tACCOUNT_ID VARCHAR ( 255 ),\n" +
//                "\tTX_HASH VARCHAR ( 255 ) NOT NULL PRIMARY KEY,\n" +
//                "\tINVOICE_NO VARCHAR ( 255 ),\n" +
//                "\tCONSUMER_NAME VARCHAR ( 255 ),\n" +
//                "\tCONSUMER_TAXES_NO VARCHAR ( 255 ),\n" +
//                "\tSELLER_NAME VARCHAR ( 255 ),\n" +
//                "\tSELLER_TAXES_NO VARCHAR ( 255 ),\n" +
//                "\tINVOICE_DATE VARCHAR ( 255 ),\n" +
//                "\tINVOICE_TYPE VARCHAR ( 255 ),\n" +
//                "\tTAXES_POINT VARCHAR ( 255 ),\n" +
//                "\tTAXES VARCHAR ( 255 ),\n" +
//                "\tPRICE VARCHAR ( 255 ),\n" +
//                "\tPRICE_PLUS_TAXES VARCHAR ( 255 ),\n" +
//                "\tINVOICE_NUMBER VARCHAR ( 255 ),\n" +
//                "\tSTATEMENT_SHEET VARCHAR ( 255 ),\n" +
//                "\tSTATEMENT_WEIGHT VARCHAR ( 255 ),\n" +
//                "\tTIMESTAMPS VARCHAR ( 255 ),\n" +
//                "\tCONTRACT_ADDRESS VARCHAR ( 255 ),\n" +
//                "\tON_CHAIN INT,\n" +
//                "\tBLOCK_HEIGHT VARCHAR ( 255 ),\n" +
//                "\tBLOCK_TIME VARCHAR ( 255 )\n" +
//                ") DEFAULT CHARSET=utf8;";
//
//        String sql2 = "CREATE TABLE invoice2 (\n" +
//                "\tSYSTEM_ID VARCHAR ( 255 ),\n" +
//                "\tREQUEST_SN VARCHAR ( 255 ),\n" +
//                "\tINVOKE_TIME datetime,\n" +
//                "\tBUSINESS_ID VARCHAR ( 255 ),\n" +
//                "\tCALLBACK_URL VARCHAR ( 255 ),\n" +
//                "\tKEY_ID VARCHAR ( 255 ),\n" +
//                "\tACCOUNT_ID VARCHAR ( 255 ),\n" +
//                "\tTX_HASH VARCHAR ( 255 ) NOT NULL PRIMARY KEY,\n" +
//                "\tINVOICE_NO VARCHAR ( 255 ),\n" +
//                "\tCONSUMER_NAME VARCHAR ( 255 ),\n" +
//                "\tCONSUMER_TAXES_NO VARCHAR ( 255 ),\n" +
//                "\tSELLER_NAME VARCHAR ( 255 ),\n" +
//                "\tSELLER_TAXES_NO VARCHAR ( 255 ),\n" +
//                "\tINVOICE_DATE VARCHAR ( 255 ),\n" +
//                "\tINVOICE_TYPE VARCHAR ( 255 ),\n" +
//                "\tTAXES_POINT VARCHAR ( 255 ),\n" +
//                "\tTAXES VARCHAR ( 255 ),\n" +
//                "\tPRICE VARCHAR ( 255 ),\n" +
//                "\tPRICE_PLUS_TAXES VARCHAR ( 255 ),\n" +
//                "\tINVOICE_NUMBER VARCHAR ( 255 ),\n" +
//                "\tSTATEMENT_SHEET VARCHAR ( 255 ),\n" +
//                "\tSTATEMENT_WEIGHT VARCHAR ( 255 ),\n" +
//                "\tTIMESTAMPS VARCHAR ( 255 ),\n" +
//                "\tCONTRACT_ADDRESS VARCHAR ( 255 ),\n" +
//                "\tON_CHAIN INT,\n" +
//                "\tBLOCK_HEIGHT VARCHAR ( 255 ),\n" +
//                "\tBLOCK_TIME VARCHAR ( 255 )\n" +
//                ") DEFAULT CHARSET=utf8;";
//
//        ConnectionParam connectionParam = new ConnectionParam(
//                "192.168.192.136",
//                "3306",
//                "ouyeel_cita",
//                "root",
//                "admin"
//        );
//
//        SqlConnector sqlConnector = new SqlConnector(connectionParam);
//        sqlConnector.openConnect();
//        if (x == 1) {
//            sqlConnector.sqlCreateTable(sql1);
//            x = 2;
//        } else {
//            sqlConnector.sqlCreateTable(sql2);
//        }
//    }

