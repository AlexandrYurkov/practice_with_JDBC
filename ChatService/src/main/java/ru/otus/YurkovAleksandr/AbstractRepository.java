package ru.otus.YurkovAleksandr;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class AbstractRepository<T> {
    private DataSource dataSource;
    private PreparedStatement psInsert;
    private List<Field> cachedFields;
    private List<Field> fields = null;
    private Class<T> cls;


    public AbstractRepository(DataSource dataSource, Class<T> cls) {
        this.dataSource = dataSource;
        this.cls = cls;
        this.prepareInsert(cls);
    }

    public void save(T entity) {
        try {
            for (int i = 0; i < cachedFields.size(); i++) {
                psInsert.setObject(i + 1, cachedFields.get(i).get(entity));
            }
            psInsert.executeUpdate();
        } catch (Exception e) {
            throw new ORMException("Что-то пошло не так при сохранении: " + entity);
        }
    }

    private void prepareInsert(Class cls) {
        if (!cls.isAnnotationPresent(RepositoryTable.class)) {
            throw new ORMException("Класс не предназначен для создания репозитория, не хватает аннотации @RepositoryTable");
        }
        String tableName = ((RepositoryTable) cls.getAnnotation(RepositoryTable.class)).title();
        StringBuilder query = new StringBuilder("insert into ");
        query.append(tableName).append(" (");
        // 'insert into users ('
        cachedFields = Arrays.stream(cls.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(RepositoryField.class))
                .filter(f -> !f.isAnnotationPresent(RepositoryIdField.class))
                .collect(Collectors.toList());
        for (Field f : cachedFields) { // TODO заменить на использование геттеров
            f.setAccessible(true);
        }
        for (Field f : cachedFields) {
            query.append(ColumName(f)).append(", ");
        }
        // 'insert into users (login, password, nickname, '
        query.setLength(query.length() - 2);
        query.append(") values (");
        // 'insert into users (login, password, nickname) values ('
        for (Field f : cachedFields) {
            query.append("?, ");
        }
        query.setLength(query.length() - 2);
        query.append(");");
        // 'insert into users (login, password, nickname) values (?, ?, ?);'
        try {
            psInsert = dataSource.getConnection().prepareStatement(query.toString());
        } catch (SQLException e) {
            throw new ORMException("Не удалось проинициализировать репозиторий для класса " + cls.getName());
        }
    }
    //чтение всех полей, для проверки когда читаем из базы.
    private void Fields (){
        if(fields == null){
            this.fields = Arrays.stream(this.cls.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(RepositoryField.class) || f.isAnnotationPresent(RepositoryIdField.class))
                .collect(Collectors.toList());
            for (Field f : fields) {
                f.setAccessible(true);
            }
        }
    }

    private int NumberOfColumns(String tableName) throws SQLException {
        ResultSet info = dataSource.getStatement().executeQuery("SELECT * FROM " + tableName + ";");
        ResultSetMetaData rsmd = info.getMetaData();
        return rsmd.getColumnCount();
    }

    public String ColumName(Field field){
        if(field.getAnnotation(RepositoryIdField.class) instanceof RepositoryIdField)
            return field.getAnnotation(RepositoryIdField.class).Colum();
        if(!field.getAnnotation(RepositoryField.class).Colum().isEmpty())
            return field.getAnnotation(RepositoryField.class).Colum();
        else
            return field.getName();
    }

    private Class[] Types(){
        Class[] types = new Class[fields.size()];
        for (int i = 0; i < fields.size(); i++){
            types[i] = fields.get(i).getType();
        }
        return types;
    }

    public T findById(int id) {
        try{
            String tableName = cls.getAnnotation(RepositoryTable.class).title();
            Fields();
            List<Object> resultObject = new ArrayList<>();
            int numberOfColumns = NumberOfColumns(tableName);
            ResultSet rs = dataSource.getStatement().executeQuery(("SELECT * FROM " + tableName + " WHERE id = " + id + ";"));
            while (rs.next()){
                for (int i = 0; i < numberOfColumns; i++){
                    resultObject.add(rs.getObject(fields.get(i).getName(), fields.get(i).getType()));
                }
            }
            Class[] types = Types();
            T result = cls.getConstructor(types)
                    .newInstance(resultObject.toArray());
            return result;
        }catch (Exception e){
            throw new ORMException("При поиске по id, что-то сломалось " + cls.getName());
        }

   }

   public List<T> findAll() {
       try {
           String tableName = cls.getAnnotation(RepositoryTable.class).title();
           List<T> result = new ArrayList<>();
           Fields();
           int numberOfColumns = NumberOfColumns(tableName);
           ResultSet rs = dataSource.getStatement().executeQuery(String.format("SELECT * FROM %s;", tableName));
           Class[] types = Types();
           while (rs.next()) {
               List<Object> resultObject = new ArrayList<>();
               for (int i = 0; i < numberOfColumns; i++) {
                   resultObject.add(rs.getObject(fields.get(i).getName(), fields.get(i).getType()));
               }
               result.add(cls.getConstructor(types)
                       .newInstance(resultObject.toArray()));
           }
           return result;
       }catch (Exception e){
           throw new ORMException("При выводе из таблицы, что-то сломалось!" +  cls.getAnnotation(RepositoryTable.class).title());
       }
   }

   public void DeleteById(int id){
        try {
            if (fields == null)
                Fields();
            String tableName = cls.getAnnotation(RepositoryTable.class).title();
            dataSource.getStatement().executeUpdate(String.format("DELETE FROM %s  WHERE id = %d ", tableName, id));
        } catch (SQLException e){
            throw new ORMException(String.format("При удалении из таблицы %s по id, что-то сломалось!",
                     cls.getAnnotation(RepositoryTable.class).title()));
        }
   }

   public void Update(T entity) throws IllegalAccessException, SQLException {
       try {
           if (!cls.isAnnotationPresent(RepositoryTable.class)) {
               throw new ORMException("Класс не предназначен для обновления репозитория, не хватает аннотации @RepositoryTable");
           }

           String tableName = cls.getAnnotation(RepositoryTable.class).title();
           Fields();
           StringBuilder query = new StringBuilder(String.format("UPDATE %s SET ", tableName));
           for (int i = 1; i < fields.size(); i++)
               query.append(ColumName(fields.get(i)) + " = ?, ");
           query.setLength(query.length() - 2);
           query.append(" WHERE " + ColumName(fields.get(0)) + " = ?;");
           psInsert = dataSource.getConnection().prepareStatement(query.toString());

           for (int i = 1; i < fields.size(); i++) {
               psInsert.setObject(i, fields.get(i).get(entity));
           }
           psInsert.setObject(fields.size(), fields.get(0).get(entity));
           psInsert.executeUpdate();
       }catch (Exception e){
           throw new ORMException(String.format("При обновлении из таблицы %s по id, что-то сломалось!",
                   cls.getAnnotation(RepositoryTable.class).title()));
       }
   }
}
