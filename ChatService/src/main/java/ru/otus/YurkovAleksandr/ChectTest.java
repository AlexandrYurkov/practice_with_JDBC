package ru.otus.YurkovAleksandr;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@RepositoryTable(title = "Test")
public class ChectTest {
    @RepositoryIdField
    private Long id;
    @RepositoryField
    private String owner_login;
    @RepositoryField
    private Integer amount;
    private int count;

    public ChectTest(Long id, String owner_login, Integer amount) {
        this.id = id;
        this.owner_login = owner_login;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public String getOwner_login() {
        return owner_login;
    }

    public Integer getAmount() {
        return amount;
    }

    public int getCount() {
        return count;
    }

    public String getTestAnnotation() {
//        String tableName = ((RepositoryTable) cls.getAnnotation(RepositoryTable.class)).title();
//        cachedFields = Arrays.stream(cls.getDeclaredFields())
//                .filter(f -> f.isAnnotationPresent(RepositoryField.class))
//                .filter(f -> !f.isAnnotationPresent(RepositoryIdField.class))
//                .collect(Collectors.toList());
//        for (Field f : cachedFields) { // TODO заменить на использование геттеров
//            f.setAccessible(true);
//        }
        List<Field> f = Arrays.stream(this.getClass().getDeclaredFields()).filter(a -> a.isAnnotationPresent(TestName.class)).toList();

        TestName a = f.get(0).getAnnotation(TestName.class);
        return a.Colum();
    }

    @Override
    public String toString() {
        return "ChectTest{" +
                "id=" + id +
                ", owner_login='" + owner_login + '\'' +
                ", amount=" + amount +
                ", count=" + count +
                '}';
    }
}
