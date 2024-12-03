package ru.otus.YurkovAleksandr;

public class ChatService {
    public static void main(String[] args) {
        System.out.println("Сервер чата запущен");
        DataSource dataSource = new DataSource("jdbc:sqlite:test.db");
////        DataSource dataSource = new DataSource("jdbc:h2:file:./date.db;MODE=PostgreSQL");
        //тест
        try {
            dataSource.connect();
            DbMigrator migrator = new DbMigrator(dataSource);
            migrator.migrate("dbinit.sql");
            AbstractRepository<ChectTest> testRepository = new AbstractRepository<>(dataSource, ChectTest.class);
            testRepository.save(new ChectTest(null, "test", 11));
            testRepository.save(new ChectTest(null, "test1", 21));
            AbstractRepository<User> userTest = new AbstractRepository<>(dataSource, User.class);
            userTest.save(new User(null, "Login", "pass", "nick"));
            userTest.save(new User(null, "Login1", "pass1", "nick1"));
            System.out.println(testRepository.findById(2));
            System.out.println(userTest.findById(2));
            testRepository.Update(new ChectTest(2L, "test18", 250));
            userTest.Update(new User(2L, "Login2", "pass2", "nick2"));
            System.out.println(testRepository.findById(2));
            System.out.println(userTest.findById(2));
            userTest.DeleteById(1);


        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (dataSource != null) {
                dataSource.close();
            }
            System.out.println("Сервер чата завершил свою работу");
        }
    }
}