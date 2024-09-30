package org.georchestra.console.integration;

import org.georchestra.console.events.RabbitmqEventsListener;
import org.georchestra.console.events.RabbitmqEventsSender;
import org.georchestra.console.mailservice.EmailFactory;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import javax.mail.MessagingException;
import java.util.List;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:/webmvc-config-test.xml" })
@TestPropertySource(properties = { "enableRabbitmqEvents = true" })
public class RabbitMqEventsIT extends ConsoleIntegrationTest {

    public static RabbitMQContainer rabbitmq = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.12"));

    public @Rule @Autowired IntegrationTestSupport support;

    @Autowired
    private CachingConnectionFactory rabbitFactory;
    @Autowired
    private RabbitmqEventsListener eventsListener;
    @Autowired
    private AmqpTemplate eventTemplate;

    private static AmqpAdmin admin;

    private static final MockEmailFactory emailFactory = new MockEmailFactory();

    @BeforeClass
    public static void setUpClass() {
        rabbitmq.start();
        System.setProperty("rabbitmqHost", "localhost");
        System.setProperty("rabbitmqPort", String.valueOf(rabbitmq.getAmqpPort()));
        System.setProperty("rabbitmqUser", rabbitmq.getAdminUsername());
        System.setProperty("rabbitmqPassword", rabbitmq.getAdminPassword());
        System.setProperty("pgsqlPort", System.getProperty("jdbc.port"));

        // This is normally configured by the geOrchestra gateway, see
        // *
        // https://github.com/georchestra/georchestra-gateway/blob/main/gateway/src/main/resources/rabbit-listener-context.xml
        // *
        // https://github.com/georchestra/georchestra-gateway/blob/main/gateway/src/main/resources/rabbit-sender-context.xml

        CachingConnectionFactory fac = new CachingConnectionFactory("localhost", rabbitmq.getAmqpPort());
        admin = new RabbitAdmin(fac);
        TopicExchange ex = new TopicExchange("OAUTH2-EXCHANGE-GATEWAY");
        admin.declareExchange(ex);
        Queue q = new AnonymousQueue();
        admin.declareQueue(q);
        Binding b = BindingBuilder.bind(q).to(ex).with("routing-console");
        admin.declareBinding(b);
    }

    @AfterClass
    public static void tearDownClass() {
        rabbitmq.stop();
    }

    @Before
    public void before() {
        // the test should success and stop everything before giving a chance to send an
        // event to the gateway via rabbitMq, mocking the object sounds good enough
        // here.
        eventsListener.setRabbitmqEventsSender(Mockito.mock(RabbitmqEventsSender.class));
        eventsListener.setEmailFactory(emailFactory);

    }

    public @Test void testReceiveEvent() {
        sendGatewayOauth2MessageCreationToRabbitMq();

        await().atMost(30, SECONDS).until(() -> {
            return emailFactory.sendNewOAuth2AccountNotificationEmailCalled;
        });
    }

    private void sendGatewayOauth2MessageCreationToRabbitMq() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("uid", UUID.randomUUID());
        jsonObj.put("subject", "OAUTH2-ACCOUNT-CREATION");
        jsonObj.put("fullName", "flup");
        jsonObj.put("localUid", "aaa");
        jsonObj.put("email", "martinh@thefiddlingcompany.com");
        jsonObj.put("organization", "thefiddlingcompany");
        jsonObj.put("providerName", "myoauth2provider");
        jsonObj.put("providerUid", "martinhoauth2");

        eventTemplate.convertAndSend("OAUTH2-EXCHANGE", "routing-gateway", jsonObj.toString());
    }

    private static class MockEmailFactory extends EmailFactory {
        public boolean sendNewOAuth2AccountNotificationEmailCalled = false;

        @Override
        public void sendNewOAuth2AccountNotificationEmail(List<String> recipients, String fullName, String localUid,
                String emailAddress, String providerName, String providerUid, String userOrg, boolean reallySend)
                throws MessagingException {
            sendNewOAuth2AccountNotificationEmailCalled = true;
        }
    }
}
