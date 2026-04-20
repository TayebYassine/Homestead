package tfagaming.projects.minecraft.homestead.tools.minecraft.threads;

import org.bukkit.scheduler.BukkitTask;

public final class TaskHandle {

	private final Object task;

	/**
	 * @param task An instance of {@link BukkitTask} or {@link io.papermc.paper.threadedregions.scheduler.ScheduledTask}.
	 */
	public TaskHandle(Object task) {
		this.task = task;
	}

	public void cancel() {
		if (task instanceof BukkitTask) {
			((BukkitTask) task).cancel();
		} else if (task instanceof io.papermc.paper.threadedregions.scheduler.ScheduledTask) {
			((io.papermc.paper.threadedregions.scheduler.ScheduledTask) task).cancel();
		}
	}

	public boolean isCancelled() {
		if (task instanceof BukkitTask) {
			return ((BukkitTask) task).isCancelled();
		} else if (task instanceof io.papermc.paper.threadedregions.scheduler.ScheduledTask scheduledTask) {
			return scheduledTask.isCancelled();
		}
		return false;
	}

	public Object getRaw() {
		return task;
	}
}