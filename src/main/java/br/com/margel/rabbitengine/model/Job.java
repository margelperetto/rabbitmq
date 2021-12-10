package br.com.margel.rabbitengine.model;

public class Job {
	private String oid;
	private String queue;
	private String msg;
	private boolean finished;
	private long created;
	private long received;
	private Priority priority = Priority.MIN;
	
	public String getOid() {
		return oid;
	}
	public String getQueue() {
		return queue;
	}
	public String getMsg() {
		return msg;
	}
	public boolean isFinished() {
		return finished;
	}
	public long getCreated() {
		return created;
	}
	public long getReceived() {
		return received;
	}
	public void setOid(String oid) {
		this.oid = oid;
	}
	public void setQueue(String queue) {
		this.queue = queue;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public void setFinished(boolean finished) {
		this.finished = finished;
	}
	public void setCreated(long created) {
		this.created = created;
	}
	public void setReceived(long received) {
		this.received = received;
	}
	public Priority getPriority() {
		return priority;
	}
	public void setPriority(Priority priority) {
		this.priority = priority;
	}
	@Override
	public String toString() {
		return "Job [oid=" + oid + ", queue=" + queue + ", msg=" + msg + ", priority=" + priority + ", finished="
				+ finished + ", created=" + created + ", received=" + received + "]";
	}
}