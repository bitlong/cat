package com.dianping.cat.consumer.state;

import java.util.Date;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.state.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.state.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.DefaultSaxParser;
import com.dianping.cat.service.ReportDelegate;
import com.dianping.cat.task.TaskManager;
import com.dianping.cat.task.TaskManager.TaskProlicy;

public class StateDelegate implements ReportDelegate<StateReport> {

	@Inject
	private TaskManager m_taskManager;

	@Override
	public void afterLoad(Map<String, StateReport> reports) {
	}

	@Override
	public void beforeSave(Map<String, StateReport> reports) {
	}

	@Override
	public String buildXml(StateReport report) {
		return report.toString();
	}

	@Override
	public boolean createHourlyTask(StateReport report) {
		Date startTime = report.getStartTime();
		String domain = report.getDomain();

		m_taskManager.createTask(startTime, domain, Constants.REPORT_SERVICE, TaskProlicy.ALL);
		m_taskManager.createTask(startTime, domain, Constants.REPORT_BUG, TaskProlicy.ALL);
		m_taskManager.createTask(startTime, domain, Constants.REPORT_HEAVY, TaskProlicy.ALL);
		m_taskManager.createTask(startTime, domain, Constants.REPORT_ALERT, TaskProlicy.ALL);
		m_taskManager.createTask(startTime, domain, Constants.REPORT_UTILIZATION, TaskProlicy.ALL);
		m_taskManager.createTask(startTime, domain, Constants.REPORT_NET_TOPOLOGY, TaskProlicy.HOULY);
		m_taskManager.createTask(startTime, domain, Constants.REPORT_ROUTER, TaskProlicy.DAILY);
		return m_taskManager.createTask(startTime, domain, StateAnalyzer.ID, TaskProlicy.ALL_EXCLUED_HOURLY);
	}

	@Override
	public String getDomain(StateReport report) {
		return report.getDomain();
	}

	@Override
	public StateReport makeReport(String domain, long startTime, long duration) {
		StateReport report = new StateReport(domain);

		report.setStartTime(new Date(startTime));
		report.setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public StateReport mergeReport(StateReport old, StateReport other) {
		StateReportMerger merger = new StateReportMerger(old);

		other.accept(merger);
		return old;
	}

	@Override
	public StateReport parseXml(String xml) throws Exception {
		StateReport report = DefaultSaxParser.parse(xml);

		return report;
	}
	
	@Override
	public byte[] buildBinary(StateReport report) {
		return DefaultNativeBuilder.build(report);
	}

	@Override
	public StateReport parseBinary(byte[] bytes) {
		return DefaultNativeParser.parse(bytes);
	}
}
