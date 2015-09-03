package reactor.spring.core.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisherAware;
import reactor.Timers;
import reactor.core.processor.BaseProcessor;
import reactor.core.processor.RingBufferWorkProcessor;
import reactor.fn.timer.Timer;
import reactor.core.processor.rb.disruptor.BlockingWaitStrategy;
import reactor.core.processor.rb.disruptor.WaitStrategy;
import reactor.core.processor.rb.disruptor.dsl.ProducerType;

/**
 * Implementation of an {@link org.springframework.core.task.AsyncTaskExecutor} that is backed by a Reactor {@link
 * RingBufferWorkProcessor}.
 *
 * @author Jon Brisbin
 * @author Stephane Maldini
 * @since 1.1, 2.1
 */
public class WorkQueueAsyncTaskExecutor extends AbstractAsyncTaskExecutor implements ApplicationEventPublisherAware {

	private final Logger log = LoggerFactory.getLogger(WorkQueueAsyncTaskExecutor.class);

	private ProducerType                      producerType;
	private WaitStrategy                      waitStrategy;
	private BaseProcessor<Runnable, Runnable> workQueue;

	public WorkQueueAsyncTaskExecutor() {
		this(Timers.globalOrNew());
	}

	public WorkQueueAsyncTaskExecutor(Timer timer) {
		super(timer);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (producerType != null && producerType == ProducerType.SINGLE) {
			this.workQueue = RingBufferWorkProcessor.create(
			  getName(),
			  getBacklog(),
			  (null != waitStrategy ? waitStrategy : new BlockingWaitStrategy())
			);
		} else {
			this.workQueue = RingBufferWorkProcessor.share(
			  getName(),
			  getBacklog(),
			  (null != waitStrategy ? waitStrategy : new BlockingWaitStrategy())
			);
		}
		if(isAutoStartup()){
			start();
		}
	}

	/**
	 * Get the {@link reactor.core.processor.rb.disruptor.dsl.ProducerType} this {@link reactor.core.processor.rb.disruptor.RingBuffer} is using.
	 *
	 * @return the {@link reactor.core.processor.rb.disruptor.dsl.ProducerType}
	 */
	public ProducerType getProducerType() {
		return producerType;
	}

	/**
	 * Set the {@link reactor.core.processor.rb.disruptor.dsl.ProducerType} to use when creating the internal {@link
	 * reactor.core.processor.rb.disruptor.RingBuffer}.
	 *
	 * @param producerType
	 * 		the {@link reactor.core.processor.rb.disruptor.dsl.ProducerType}
	 */
	public void setProducerType(ProducerType producerType) {
		this.producerType = producerType;
	}

	/**
	 * Get the {@link reactor.core.processor.rb.disruptor.WaitStrategy} this {@link reactor.core.processor.rb.disruptor.RingBuffer} is using.
	 *
	 * @return the {@link reactor.core.processor.rb.disruptor.WaitStrategy}
	 */
	public WaitStrategy getWaitStrategy() {
		return waitStrategy;
	}

	/**
	 * Set the {@link reactor.core.processor.rb.disruptor.WaitStrategy} to use when creating the internal {@link
	 * reactor.core.processor.rb.disruptor.RingBuffer}.
	 *
	 * @param waitStrategy
	 * 		the {@link reactor.core.processor.rb.disruptor.WaitStrategy}
	 */
	public void setWaitStrategy(WaitStrategy waitStrategy) {
		this.waitStrategy = waitStrategy;
	}

	@Override
	protected BaseProcessor<Runnable, Runnable> getProcessor() {
		return workQueue;
	}

}
