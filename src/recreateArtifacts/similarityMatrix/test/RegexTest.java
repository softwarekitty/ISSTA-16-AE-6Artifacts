package recreateArtifacts.similarityMatrix.test;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import recreateArtifacts.similarityMatrix.Regex;
import recreateArtifacts.similarityMatrix.row.cell.MatchTask;

// keep this proof
public class RegexTest {

	private static final int nThreads = 2;
	private static final int MS_MAX = 100;

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		Regex goodRegex = new Regex("(a+)b");
		Regex badRegex = new Regex("(a+a+)+b");
		String input = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
		MatchTask mt_good = new MatchTask(goodRegex, input);
		MatchTask mt_bad = new MatchTask(badRegex, input);
		// testNormalTimeout(mt_good, MS_MAX);
		testCancellingTimeout(mt_good, MS_MAX);
		// testNormalTimeout(mt_bad, MS_MAX);
		testCancellingTimeout(mt_bad, MS_MAX);
	}

	/**
	 * this seems slightly better because the cancelled flag on the future is
	 * tied to it having timed out, whereas just using timeouts you may have to
	 * use the exception per future, which is kind of inside the executor
	 * service, or not in line with that interface somehow
	 */
	private static void testCancellingTimeout(MatchTask mt, int nMS) throws InterruptedException, ExecutionException {
		long beginTime = System.currentTimeMillis();
		ExecutorService service = Executors.newFixedThreadPool(nThreads);
		ScheduledExecutorService canceller = Executors.newSingleThreadScheduledExecutor();
		System.out.println("timeElapsed after setup: " + (System.currentTimeMillis() - beginTime));

		Future<Boolean> timeoutMatch = executeTask(mt, nMS, service, canceller);

		System.out.println("timeoutMatch isCancelled: " + timeoutMatch.isCancelled() + " isDone: "
				+ timeoutMatch.isDone() + " timeElapsed: " + (System.currentTimeMillis() - beginTime));
		try {
			Boolean b = timeoutMatch.get();

			// maybe never get here...
			System.out.println("result from cancelling timeout: " + b);
		} catch (CancellationException ce) {
			System.out.println("cancelled");
		}

		System.out.println("timeoutMatch isCancelled: " + timeoutMatch.isCancelled() + " isDone: "
				+ timeoutMatch.isDone() + " timeElapsed: " + (System.currentTimeMillis() - beginTime));
		service.shutdown();
		canceller.shutdown();
		System.out.println("final timeElapsed: " + (System.currentTimeMillis() - beginTime));
	}

	private static void testNormalTimeout(MatchTask mt, int nMS) throws InterruptedException, ExecutionException {
		long beginTime = System.currentTimeMillis();
		ExecutorService e = getCustomExecutorService(nThreads,Thread.NORM_PRIORITY);
		System.out.println("timeElapsed after setup: " + (System.currentTimeMillis() - beginTime));

		Future<Boolean> f = e.submit(mt);
		try {
			System.out.println("result from normal timeout: " + f.get(nMS, TimeUnit.MILLISECONDS));
		} catch (TimeoutException te) {
			System.out.println("timed out");
		}
		System.out.println("isCancelled: " + f.isCancelled() + " isDone: " + f.isDone());
		e.shutdown();
		System.out.println("final timeElapsed: " + (System.currentTimeMillis() - beginTime));
	}


	// http://stackoverflow.com/questions/4819855/time-limit-on-individual-threads-with-executorservice
	private static Future<Boolean> executeTask(Callable<Boolean> c, long timeoutMS, ExecutorService service,
			ScheduledExecutorService canceller) {
		final Future<Boolean> future = service.submit(c);
		canceller.schedule(new Callable<Void>() {
			public Void call() {
				future.cancel(true);
				return null;
			}
		}, timeoutMS, TimeUnit.MILLISECONDS);
		return future;
	}
	//Thread.MIN_PRIORITY
	private static ExecutorService getCustomExecutorService(int nThreads, int priority) {
		ExecutorService eService = Executors.newFixedThreadPool(nThreads, new ThreadFactory() {
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setPriority(priority);
				t.setDaemon(true);
				return t;
			}
		});
		return eService;
	}

}
