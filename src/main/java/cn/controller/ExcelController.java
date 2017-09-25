package cn.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import cn.model.Config;
import cn.service.ExcelService;
import cn.utils.utils;

@Controller
public class ExcelController {
	@Autowired
	private ExcelService excelService;
	Config config = null;
	Properties properties;
	
	@RequestMapping("/excelHtmlUpdate")
	public String list(Model model) {
		return "excelselect";
	}

	@RequestMapping("/zhengqiuquan")
	public String list2(Model model) {
		return "zhengqiuquan";
	}
	/**
	 * @param excelName: 表名 内容
	 * @param resoure:	  导入的服务器
	 * @param excel:     excel目录库
	 * @param userbane:  用户名
	 * @param delete: 	  是否可删除
	 * 
	 * */
	@RequestMapping(value = "/excelUpdate", method = RequestMethod.POST)
	public synchronized String excelUpdate(Model model, String excelName, String[] resoure, String excel,
			String username) {
		
//		String isDelete = req.getParam("delete");
		//请输入用户名和表名;
		if ("".equals(excelName) || "".equals(username) || excelName == null || username == null) {
			return ""; 
		}
		String[] excel_arr = excelName.split("\r\n");
		List<String> excelNames = new LinkedList<String>();
		for (int i = 0; i < excel_arr.length; i++) {
			excelNames.add(excel_arr[i]);
		}
		StringBuffer urlLink = new StringBuffer();
		try {
			//加载properties
			properties = utils.loadProperties("dbconf.properties");
			for (int i = 0; i < resoure.length; i++) {
				urlLink.append("<br>:" + resoure[i] + "服:");
				config = new Config(properties, resoure[i], excel);
				System.out.println("用户名：" + username + "!数据库:" + resoure[i] + "!excel版本:" + excel);
				urlLink.append(excelService.converter(config, excelNames, false));
			}
			urlLink.append("<br><a href=\"http://build.sango.hoolai.com/sqlup/rc\">更新{rc}数据库文件</a>");
			urlLink.append("<br><a href=\"http://build.sango.hoolai.com/sqlup/dev\">更新{dev}数据库文件</a>");
			urlLink.append("<br><a href=\"http://build.sango.hoolai.com/sqlup/dev_1.5.0\">更新{dev_1.5.0}数据库文件</a>");		
			
			File f = createLogFile();
			if(f == null){
				return null;
			}
			FileWriter fw = new FileWriter(f,true);
			
			fw.write(utils.getTime() + ":" + username + ":" + urlLink.toString());
			fw.write("\r\n");
			System.out.println(utils.getTime() + ":" + username + ":");
			fw.flush();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw, true));
			utils.writererror(config, username);
			System.out.println(username + "插入表时失败：");
			return "excelresult";
		}
		model.addAttribute("excel", urlLink.toString());
		return "excelresult";
	}
	
	
	private File createLogFile() {	
		File file = new File(config.excelDirectoty + "\\excellog\\");
		if(!file.exists()){
			file.mkdirs();
		}
		File file1 = new File(config.excelDirectoty + "\\excellog\\" + utils.getTime1() + "excellog.txt");
		if(!file1.exists()){
			try {
				file1.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file1;
		
	}

	/**
	 * @param excelName: 表名 内容
	 * @param resoure:	  导入的服务器
	 * @param excel:     excel目录库
	 * @param userbane:  用户名
	 * @param delete: 	  是否可删除
	 * 
	 * */
	@RequestMapping(value = "/excelUpdate1", method = RequestMethod.POST)
	public synchronized String excelUpdate1(Model model, String excelName, String[] resoure, String excel,
			String username, String delete, HttpSession httpSession) {
		//用户名和表名是否为空
		if ("".equals(excelName) || "".equals(username) || excelName == null || username == null) {
			return "";
		}
		String[] excel_Arr = excelName.split("\r\n");
		List<String> excelNames = new LinkedList<String>();
		for (int i = 0; i < excel_Arr.length; i++) {
			excelNames.add(excel_Arr[i]);
		}
		StringBuffer urlLink = new StringBuffer();
		try {
			properties = utils.loadProperties("dbconf.properties");
			for (int i = 0; i < resoure.length; i++) {
				urlLink.append(":" + resoure[i] + "服:");
				config = new Config(properties, resoure[i], excel);//多个config对象 
				System.out.println("用户名：" + username + "!数据库:" + resoure[i] + "!excel版本:" + excel);
				urlLink.append(excelService.converter(config, excelNames, "1".equals(delete)));
			}						
			urlLink.append("<br><a href=\"http://build.sango.hoolai.com/sqlup/rc\">更新{rc}数据库文件</a>");
			urlLink.append("<br><a href=\"http://build.sango.hoolai.com/sqlup/dev\">更新{dev}数据库文件</a>");
			urlLink.append("<br><a href=\"http://build.sango.hoolai.com/sqlup/gve\">更新{gve}数据库文件</a>");
			FileWriter fw = new FileWriter(config.excelDirectoty + "\\" + "excellog.txt", true);
			fw.write(utils.getTime() + ":" + username + ":" + urlLink.toString());
			fw.write("\r\n");
			System.out.println(utils.getTime() + ":" + username + ":");
			// 刷新缓冲区
			fw.flush();
			// 关闭文件流对象
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw, true));
			utils.writererror(config, username);
			System.out.println(username + "插入表时失败：");
			return "excelresult";
		}
		model.addAttribute("excel", urlLink.toString());
		return "excelresult";
	}

	@RequestMapping("/livingHtml")
	public synchronized String living(Model model, String dbname) {
		Config config = null;
		String s = "";
		try {
			config = new Config(dbname);
			s = excelService.Living(config);
			model.addAttribute("excel", s);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "excelresult";
	}
}