package metricsdemo;

import org.mule.modules.metrics.MetricsConnector;
import org.mule.modules.metrics.spring.GaugeBean;
import org.mule.transport.vm.VMConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyBean extends GaugeBean {

	private static Logger log = LoggerFactory.getLogger(MetricsConnector.class);

	public MyBean() {

		this.setCategory("VMQueue");
		this.setName("Pending Jobs");

	}

	public Integer returnValue() {

		VMConnector vmConnector = (VMConnector) this.getMuleContext()
				.getRegistry().lookupConnector("VM");

		int queueSize = vmConnector.getQueueManager().getQueueSession()
				.getQueue("process.demo").size();

		log.info("Queue size:" + Integer.toString(queueSize));

		return new Integer(queueSize);
	}
}
