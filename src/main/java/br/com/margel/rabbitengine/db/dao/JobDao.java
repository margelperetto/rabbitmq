package br.com.margel.rabbitengine.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.margel.rabbitengine.db.Database;
import br.com.margel.rabbitengine.model.Job;
import br.com.margel.rabbitengine.model.Priority;

public class JobDao {

	private static final String INSERT_SQL = "INSERT INTO JOBS"
			+ "(OID, QUEUE, MSG, PRIORITY, FINISHED, DTCREATED)"
			+ "VALUES(?,?,?,?,?,?);";
	private static final String FINISH_JOB = "UPDATE JOBS SET FINISHED=true, DTRECEIVED=? WHERE OID=? AND FINISHED=false";
	private static final String LOAD_WAITING_JOBS = "SELECT * FROM JOBS WHERE FINISHED = false ORDER BY DTCREATED";

	public int insert(Job job) throws SQLException {
		try(
				Connection conn = Database.getInstance().newConnection();
				PreparedStatement ppst = conn.prepareStatement(INSERT_SQL);
				){
			ppst.setString(1, job.getOid());
			ppst.setString(2, job.getQueue());
			ppst.setString(3, job.getMsg());
			ppst.setInt(4, job.getPriority().ordinal());
			ppst.setBoolean(5, job.isFinished());
			ppst.setLong(6, job.getCreated());
			return ppst.executeUpdate();
		}
	}

	public int finishJob(String oid, long dtFinished) throws SQLException {
		try(
				Connection conn = Database.getInstance().newConnection();
				PreparedStatement ppst = conn.prepareStatement(FINISH_JOB);
				){
			ppst.setLong(1, dtFinished);
			ppst.setString(2, oid);
			return ppst.executeUpdate();
		}
	}

	public List<Job> getWaitingJobs() throws SQLException{
		try(
				Connection conn = Database.getInstance().newConnection();
				PreparedStatement ppst = conn.prepareStatement(LOAD_WAITING_JOBS);
				ResultSet rs = ppst.executeQuery();
				){
			List<Job> jobs = new ArrayList<>();
			while(rs.next()) {
				Job job = new Job();
				job.setOid(rs.getString("OID"));
				job.setQueue(rs.getString("QUEUE"));
				job.setMsg(rs.getString("MSG"));
				job.setPriority(Priority.values()[rs.getInt("PRIORITY")]);
				job.setFinished(rs.getBoolean("FINISHED"));
				job.setCreated(rs.getLong("DTCREATED"));
				job.setReceived(rs.getLong("DTRECEIVED"));
				jobs.add(job);
			}
			return jobs;
		}
	}
}
