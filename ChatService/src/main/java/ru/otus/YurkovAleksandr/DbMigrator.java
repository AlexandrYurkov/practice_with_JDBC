package ru.otus.YurkovAleksandr;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DbMigrator {
    private DataSource dataSource;
    private List<String> list = new ArrayList<>();
    //private String path = "/Users/aleksandryurkov/Desktop/pro-java-db-interaction/dbinit.sql";
    private List<String> checkList = new ArrayList<>();

    public DbMigrator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void migrate(String nameFile) throws IOException, SQLException {

        // читаем файл dbinit.sql
        FileReader reader = new FileReader(nameFile);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            line = line.trim();
            if(!line.isEmpty() && !list.contains(line))
                list.add(line);
        }
        checkInit();
        init();
    }

    public void init() throws SQLException {
        dataSource.getConnection().setAutoCommit(false);
        if(!list.isEmpty())
            for (String sql : list)
                dataSource.getStatement().executeUpdate(sql);
        dataSource.getConnection().setAutoCommit(true);
    }

    public void checkInit() throws SQLException {
        // SELECT * FROM sqlite_master where type='table'; Или поле sql содержит запрос без ';'. Для SQLite
        // SELECT table_name FROM information_schema.tables WHERE table_schema='public';  Для Postgres
        ResultSet set = dataSource.getStatement().executeQuery("SELECT * FROM sqlite_master where type='table';");
        while (set.next())
            checkList.add(set.getString("sql") + ";");
        for (String check : checkList)
            for (int i = 0; i < list.size(); i++ )
                if(list.get(i).equalsIgnoreCase(check)) {
                    list.remove(i);
                    i = 0;

                }
    }


}
