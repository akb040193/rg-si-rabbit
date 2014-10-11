package rube.complicated;

import com.google.common.base.Joiner;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.amqp.Amqp;
import org.springframework.integration.dsl.jms.Jms;


import static java.util.stream.Collectors.toList;

@Configuration
@ImportResource("classpath:/rube/complicated/broker.xml")
public class EchoFlowOutBound {

	@Autowired
	private AmqpTemplate amqpTemplate;

	@Bean
	public IntegrationFlow toOutboundQueueFlow() {
		return IntegrationFlows.from("requestChannel")
				.split(s -> s.applySequence(true).get().getT2().setDelimiters("\\s"))
				.handle(Amqp.outboundGateway(amqpTemplate))
				.resequence()
				.aggregate(aggregate ->
						aggregate.outputProcessor(g ->
								Joiner.on(" ").join(g.getMessages()
										.stream()
										.map(m -> (String) m.getPayload()).collect(toList())))
						, null)
				.get();
	}
}