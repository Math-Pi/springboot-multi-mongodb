# SpringBoot的学习笔记（十二）：多数据源 MongoDB 

## 一、pom.xml引入Maven依赖

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-mongodb</artifactId>
    </dependency>
</dependencies>
```

## 二、application.properties配置多数据源

```properties
spring.application.name=springboot-multi-mongodb

mongodb.primary.uri=mongodb://127.0.0.1:27017
mongodb.primary.database=primary
mongodb.secondary.uri=mongodb://127.0.0.1:27017
mongodb.secondary.database=secondary
```

## 三、读取配置文件数据源信息

- 读取前缀为“mongodb”的配置信息

```java
@ConfigurationProperties(prefix = "mongodb")
public class MultipleMongoProperties {
	private MongoProperties primary = new MongoProperties();
	private MongoProperties secondary = new MongoProperties();
	//省略getter、setter方法
}

```

## 四、配置数据源

### 1.PrimaryMongoConfig

```java
@Configuration
@EnableConfigurationProperties(MultipleMongoProperties.class)//使@ConfigurationProperties注解的类生效。
@EnableMongoRepositories(basePackages = "com.multimongodb.repository.primary",
                         mongoTemplateRef = "primaryMongoTemplate")
public class PrimaryMongoConfig {
}
```

### 2.SecondaryMongoConfig

```java
@Configuration
@EnableConfigurationProperties(MultipleMongoProperties.class)
@EnableMongoRepositories(basePackages = "com.multimongodb.repository.secondary",
						mongoTemplateRef = "secondaryMongoTemplate")
public class SecondaryMongoConfig {
}
```

### 3.MultipleMongoConfig配置类

```java
@Configuration
public class MultipleMongoConfig {
	@Autowired
    private MultipleMongoProperties mongoProperties;
	@Primary
	@Bean(name = "primaryMongoTemplate")
	public MongoTemplate primaryMongoTemplate() throws Exception {
		return new MongoTemplate(primaryFactory(this.mongoProperties.getPrimary()));
	}
	@Bean
	@Qualifier("secondaryMongoTemplate")
	public MongoTemplate secondaryMongoTemplate() throws Exception {
        return new MongoTemplate(secondaryFactory(this.mongoProperties.getSecondary()));
	}
	@Bean
    @Primary
	public MongoDbFactory primaryFactory(MongoProperties mongo) throws Exception {
		MongoClient client = new MongoClient(new MongoClientURI(mongoProperties.getPrimary().getUri()));
		return new SimpleMongoDbFactory(client, mongoProperties.getPrimary().getDatabase());
	}
	@Bean
	public MongoDbFactory secondaryFactory(MongoProperties mongo) throws Exception {
		MongoClient client = new MongoClient(new MongoClientURI(mongoProperties.getSecondary().getUri()));
		return new SimpleMongoDbFactory(client, mongoProperties.getSecondary().getDatabase());
	}
}
```

## 五、继承MongoRepository 接口

- 继承了 MongoRepository 会默认实现很多基本的增删改查

### 1.PrimaryRepository

```java
public interface PrimaryRepository extends MongoRepository<User, String> {
}
```

### 2.SecondaryRepository

```java
public interface SecondaryRepository extends MongoRepository<User, String> {
}
```

## 六、实体类

```java
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String  id;
    private String userName;
    private String passWord;
	//省略getter、setter方法	
}
```

## 七、测试类

```java
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
```
