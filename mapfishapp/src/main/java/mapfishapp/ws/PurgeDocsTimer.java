package mapfishapp.ws;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

public class PurgeDocsTimer {
	private static final Timer TIMER = new Timer("Purge_mapfishapp_docs", true);
	private static CopyOnWriteArrayList<Runnable> tasks = new CopyOnWriteArrayList<Runnable>();
	private static boolean initialized = false;

	private static void intialize() {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				for (Runnable task : tasks) {
					task.run();
				}
			}
		};
		TIMER.scheduleAtFixedRate(task, Calendar.getInstance().getTime(), 60000);
		initialized = true;
	}

	public static synchronized void startPurgeDocsTimer(
			final Runnable purgeMethod) {
		if (!initialized)
			intialize();
		if(!tasks.contains(purgeMethod))
			tasks.add(purgeMethod);
	}
}
