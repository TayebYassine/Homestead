package tfagaming.projects.minecraft.homestead.tools.minecraft.papermc;

import org.bukkit.scheduler.BukkitTask;

public class TaskHandle {

	private final Object task;

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