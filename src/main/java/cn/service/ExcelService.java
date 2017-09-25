package cn.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import cn.dao.DataOperation;
import cn.model.Config;
import cn.model.Ident;
import cn.model.Line;
import cn.model.Suffix;
import cn.utils.WorkbookUtil;
import cn.utils.utils;

@Service
public class ExcelService {

	private boolean isDelete;
	private StringBuffer sbMain = new StringBuffer();

	public boolean isDelete() {
		return isDelete;
	}

	public void setDelete(boolean isDelete) {
		this.isDelete = isDelete;
	}

	/**
	 * @param excelNames 表名 内容
	 **/

	public Object converter(Config config, List<String> excelNames, boolean bool) throws IOException {
		if (!utils.svnup(config)) {
			return "SVN更新失败!";
		}
		return start(config, excelNames, bool);
	}

	@SuppressWarnings("unchecked")
	public String start(Config config, Object excels, boolean bool) {
		this.isDelete = bool;
		List<String> excel = (List<String>) excels;
		return importExcelToSQL(config, excel);
	}

	/**
	 * @param excels 表名 内容
	 **/
	private String importExcelToSQL(Config config, List<String> excels) {
		DataOperation dataOperation = new DataOperation(config);
		StringBuffer sb = new StringBuffer();
		sbMain.delete(0, sbMain.length());	//sbmain -> StringBuffer,清空
		for (String lineStr : excels) {
			int row = 2;
			if (lineStr.split(":").length > 1) {
				row = Integer.valueOf(lineStr.split(":")[1]);
			}
			String[] excelNames = lineStr.split(":")[0].split(",");

			if (excelNames.length >= 2) {			//判断是否分表，现在用处不大
				String tableName = "";
				if (excelNames[0].indexOf("t_s_droptemplate") != -1) {
					tableName = "t_s_droptemplate";
				} else {
					tableName = excelNames[0];
				}
				List<Object> workbookList = new ArrayList<Object>();
				try {
					for (int i = 0; i < excelNames.length; i++) {
						Object workbook = WorkbookUtil.creatWorkbook(config.excelDirectoty, excelNames[i]);
						workbookList.add(workbook);//存储excel的workbook对象
					}
					if (DataOperation.map.containsKey(tableName)) {	//无主键导入
						excelUpdateDBNoKey(dataOperation, row, tableName, workbookList, sb,
								WorkbookUtil.findSuffix(config.excelDirectoty, tableName));
					} else {
						excelUpdateDB(dataOperation, row, tableName, workbookList, sb,
								WorkbookUtil.findSuffix(config.excelDirectoty, tableName));
					}
					sb.append("导入" + excelNames[0] + "表成功!<br>");
					sbMain.append("导入" + excelNames[0] + "表成功!<br><br>");
				} catch (Exception ee) {
					ee.printStackTrace();
					StringWriter sw = new StringWriter();
					ee.printStackTrace(new PrintWriter(sw, true));
					return sw.toString().toString();
				} finally {
					WorkbookUtil.close();
				}
			} else {
				String tableName = "";
				if (excelNames[0].indexOf("t_s_droptemplate") != -1) {
					tableName = "t_s_droptemplate";
				} else {
					tableName = excelNames[0];
				}
				try {
					Object workbook = WorkbookUtil.creatWorkbook(config.excelDirectoty, excelNames[0]);
					List<Object> workbookList = new ArrayList<Object>();
					workbookList.add(workbook);
					if (DataOperation.map.containsKey(tableName)) {
						excelUpdateDBNoKey(dataOperation, row, tableName, workbookList, sb,
								WorkbookUtil.findSuffix(config.excelDirectoty, excelNames[0]));//后缀
					} else {
						excelUpdateDB(dataOperation, row, tableName, workbookList, sb,
								WorkbookUtil.findSuffix(config.excelDirectoty, excelNames[0]));
					}
					sb.append("导入" + excelNames[0] + "表成功!<br>");
					sbMain.append("导入" + excelNames[0] + "表成功!<br><br>");
				} catch (Exception e) {
					e.printStackTrace();
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw, true));
					sb.append("导入" + excelNames[0] + "表失败!" + sw.toString()).toString();
					sbMain.append("导入" + excelNames[0] + "表失败!<br>");
				} finally {
					WorkbookUtil.close();
				}
			}
		}
		sb.insert(0, sbMain.toString() + "<br><br>");
		return sb.toString();
	}
	
	/**
	 * @param DataOperation 数据操作
	 * @param startLine row开始行数
	 * @param workbookList excel工作对象列表
	 * @param sb	记录信息
	 * @param suffix 后缀名字
	 * */
	@SuppressWarnings("unchecked")
	private int excelUpdateDBNoKey(DataOperation dataOperation, int startLine, String Name, List<Object> workbookList,
			StringBuffer sb, Suffix suffix) throws Exception {
		String tableName = Name.split("@")[0];		//
		List<Line> diffLine = new ArrayList<Line>();
		Map<String, List<Line>> excelData = new HashMap<String, List<Line>>();
		dataOperation.selectTablePri(tableName);
		for (int i = 0; i < workbookList.size(); i++) {
			if (suffix == Suffix.csv) {
				dataOperation.readCsvData(tableName, (List<List<String>>) workbookList.get(i), startLine, excelData);
				System.out.println("获取到Csv数据");
			} else {
				dataOperation.readExcelData(tableName, (Workbook) workbookList.get(i), startLine, excelData, sb);
				System.out.println("获取到Excel数据");
			}
		}
		int deleteNum = 0;
		int insertNum = 0;
		Map<String, List<Line>> DBData = dataOperation.readDBData(tableName);
		System.out.println("获取到DB数据");
		if (this.isDelete) {
			for (Entry<String, List<Line>> entry : DBData.entrySet()) {
				if (!excelData.containsKey(entry.getKey())) {
					entry.getValue().get(0).setIdent(Ident.DELECT);
					diffLine.add(entry.getValue().get(0));
				}
			}
		}
		for (Entry<String, List<Line>> entry : excelData.entrySet()) {
			if (!DBData.containsKey(entry.getKey())) {
				if ("".equals(entry.getKey())) {
					continue;
				}
				for (int i = 0; i < entry.getValue().size(); i++) {
					entry.getValue().get(i).setIdent(Ident.INSERT);
					diffLine.add(entry.getValue().get(i));
					insertNum++;
				}
			} else {
				for (int i = 0; i < entry.getValue().size(); i++) {
					if (!entry.getValue().get(i).equalList(DBData.get(entry.getKey()))) {
						Line line = entry.getValue().get(i).clon();
						line.setIdent(Ident.DELECT);
						deleteNum++;
						diffLine.add(line);
						for (int j = 0; j < entry.getValue().size(); j++) {
							entry.getValue().get(j).setIdent(Ident.INSERT);
							diffLine.add(entry.getValue().get(j));
							insertNum++;
						}
						break;
					}
				}
			}
		}
		System.out.println("对比1ok");
		System.out.println(diffLine.size());
		List<String> sqlList = generateSql(diffLine);
		// 执行插入语句
		int m = dataOperation.excute(sqlList, sb);
		List<String> sqls = generateSql1(diffLine);
		sb.append("删除" + deleteNum + "条记录！ 添加" + insertNum + "条记录！");
		sb.append("{@!");
		for (int i = 0; i < sqls.size(); i++) {
			sb.append("<br>" + sqls.get(i));
		}
		sb.append("!@}");
		System.out.println("import " + tableName + " success");
		return m;
	}

	@SuppressWarnings("unchecked")
	private Integer excelUpdateDB(DataOperation dataOperation, int startLine, String Name, List<Object> workbookList,
			StringBuffer sb, Suffix suffix) throws Exception {
		// 生成sql语句
		String tableName = Name.split("@")[0];
		Map<String, List<Line>> excelData = new HashMap<String, List<Line>>();//主键，行信息
		dataOperation.selectTablePri(tableName);//在数据库中找主键
		for (int i = 0; i < workbookList.size(); i++) {//循环excel表
			if (suffix == Suffix.csv) {
				dataOperation.readCsvData(tableName, (List<List<String>>) workbookList.get(i), startLine, excelData);
				System.out.println("获取到Csv数据");
			} else {
				dataOperation.readExcelData(tableName, (Workbook) workbookList.get(i), startLine, excelData, sb);
				System.out.println("获取到Excel数据");
			}
		}
		// 字段，record信息
		Map<String, List<Line>> DBData = dataOperation.readDBData(tableName);
		System.out.println("获取到DB数据");
		List<Line> diffLine = dataCompare(excelData, DBData, sb); //对比数据库和excel信息，设置操作等
		List<String> sqlList = generateSql(diffLine);
		// 执行插入语句
		int m = 0;
		try {
			m = dataOperation.excute(sqlList, sb);
		} catch (Exception e) {
			Map<String, List<Line>> DBData1 = dataOperation.readDBData(tableName);
			System.out.println("获取到DB数据");
			List<Line> diffLine1 = dataCompare(excelData, DBData1, sb);
			List<String> sqlList1 = generateSql(diffLine1);
			try {
				m = dataOperation.excuteOne(sqlList1);
			} catch (Exception e1) {
				e1.printStackTrace();
				StringWriter sw = new StringWriter();
				e1.printStackTrace(new PrintWriter(sw, true));
				throw new Exception(sw.toString());
			}
			e.printStackTrace();
		}
		List<String> sqls = generateSql1(diffLine);
		sb.append("{@!");
		for (int i = 0; i < sqls.size(); i++) {
			sb.append("<br>" + sqls.get(i));
		}
		sb.append("!@}");
		System.out.println("import " + tableName + " success");
		return m;
	}

	private List<Line> dataCompare(Map<String, List<Line>> excelData, Map<String, List<Line>> DBData, StringBuffer sb) {
		int insertNum = 0;
		int updateNum = 0;
		List<Line> diffLine = new ArrayList<Line>();
		if (this.isDelete) {
			for (Entry<String, List<Line>> entry : DBData.entrySet()) {
				if (!excelData.containsKey(entry.getKey())) {	//excel表数据字段不包含 数据库字段
					entry.getValue().get(0).setIdent(Ident.DELECT);//设置 删除标记
					diffLine.add(entry.getValue().get(0));	//记录数据库信息的值
				}
			}
		}
		for (Entry<String, List<Line>> entry : excelData.entrySet()) {
			if (!DBData.containsKey(entry.getKey())) {	//数据库表数据字段不包含 excel字段
				if ("".equals(entry.getKey())) {
					continue;
				}
				entry.getValue().get(0).setIdent(Ident.INSERT); //设置插入标记
				diffLine.add(entry.getValue().get(0));
				insertNum++;
			} else {
				if (!entry.getValue().get(0).equalList(DBData.get(entry.getKey()))) { //值不相等，更新标识
					entry.getValue().get(0).setIdent(Ident.UPDATE);
					diffLine.add(entry.getValue().get(0));
					updateNum++;
				}
			}
		}
		System.out.println("对比1ok");
		System.out.println(diffLine.size());
		sb.append("插入:" + insertNum + "条!更新:" + updateNum + "条!");
		sbMain.append("插入:" + insertNum + "条!更新:" + updateNum + "条!");
		System.out.println("插入:" + insertNum + "条!更新:" + updateNum + "条!");
		return diffLine;
	}

	private static List<String> generateSql1(List<Line> diffLine) {
		List<String> sqlList = new LinkedList<String>();
		for (int i = 0; i < diffLine.size(); i++) {
			sqlList.add(diffLine.get(i).generateSql1());
		}
		return sqlList;
	}

	/**
	 * 生成sql语句
	 * */
	private static List<String> generateSql(List<Line> diffLine) {
		List<String> sqlList = new LinkedList<String>();
		for (int i = 0; i < diffLine.size(); i++) {
			sqlList.add(diffLine.get(i).generateSql());
		}
		return sqlList;
	}

	public String Living(Config config) throws Exception {
		StringBuffer sb = new StringBuffer();
		DataOperation dataOperation = new DataOperation(config);
		Map<Integer, String> excel1 = dataOperation
				.excuteReturn("select MonsterId,NpcArmyId from t_s_cwmonstertemplate");
		Map<Integer, String> excel2 = dataOperation
				.excuteReturn("select TemplateId,LivingIds from t_s_npcarmytemplate");
		Map<Integer, String> excel3 = dataOperation.excuteReturn(
				"select TemplateId,(Hp/6+(case when PhyAttack > MagAttack then PhyAttack else MagAttack end)+MagDefence+PhyDefence+Crit+Parry+Toughness+Mortar) from t_s_livingtemplate");
		List<String> sqllist = new LinkedList<String>();
		for (Entry<Integer, String> table : excel1.entrySet()) {
			int sum = 0;
			try {
				String livingIds = excel2.get(Integer.valueOf(table.getValue()));
				String[] livingid = livingIds.split(",");
				StringBuffer ss = new StringBuffer();
				ss.append("t_s_npcarmytemplate表id为:" + table.getValue() + "-livingId(");
				boolean bool = true;
				for (int i = 0; i < livingid.length; i++) {
					try {
						sum = sum + (int) (double) Double.valueOf(excel3.get(Integer.valueOf(livingid[i])));
					} catch (Exception e) {
						ss.append(livingid[i] + ",");
						bool = false;
					}
				}
				if (bool) {
					String s = "update t_s_cwmonstertemplate set AdviseFightCapacity=" + sum + " where MonsterId="
							+ table.getKey();
					sqllist.add(s);
				} else {
					ss.delete(ss.length() - 1, ss.length());
					ss.append(")找不到!<br>");
					System.out.println(ss.toString());
					sb.append(ss.toString());
				}
			} catch (Exception e) {
				System.out.println("t_s_npcarmytemplate表没有id：" + table.getValue() + "<br>");
				sb.append("t_s_npcarmytemplate表没有id：" + table.getValue() + "<br>");
			}
		}
		int m = dataOperation.excute(sqllist, sb);
		return sb.toString() + "操作成功，共改变了" + m + "条数据！";
	}
}
