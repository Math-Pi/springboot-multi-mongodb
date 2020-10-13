package com.multimongodb.repository;

import com.multimongodb.model.User;
import com.multimongodb.repository.primary.PrimaryRepository;
import com.multimongodb.repository.secondary.SecondaryRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest
public class MultiMongoDBeTest {
    @Autowired
    private PrimaryRepository primaryRepository;
    @Autowired
    private SecondaryRepository secondaryRepository;
    @Test
    public void TestSave() {
        primaryRepository.save(new User("Tom", "123"));
        secondaryRepository.save(new User("Jenny", "122"));
        //primary数据库操作
        List<User> users1 = primaryRepository.findAll();
        for (User user : users1) {
            System.out.println("primary："+user.toString());
        }
        //secondary数据库操作
        List<User> users2 = secondaryRepository.findAll();
        for (User user : users2) {
            System.out.println("secondary："+user.toString());
        }
    }
    @Test
    public void deleteAll() {
        primaryRepository.deleteAll();
        secondaryRepository.deleteAll();
    }
}
