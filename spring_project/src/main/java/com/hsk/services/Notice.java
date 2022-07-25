package com.hsk.services;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;

import com.hsk.interfaces.ServiceRule;
import com.hsk.utils.ProjectUtils;

@Service
public class Notice implements ServiceRule{
	@Autowired
	private SqlSessionTemplate session;
	@Autowired
	private ProjectUtils pu;
	
	public Notice() {}
	
	public void backController(int serviceCode, ModelAndView mav) {
		try {
			if(this.pu.getAttribute("accessInfo")!=null) {
				switch(serviceCode) {
				case 0: this.entrance(mav);break;
				default:
				}
			} else { mav.setViewName("index"); }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void entrance(ModelAndView mav) {
		mav.setViewName("msg");
	}
	
	public void backController(int serviceCode, Model model) {
		try {
			if(this.pu.getAttribute("accessInfo")!=null) {
				switch(serviceCode) {
				case 0: break;
				default:
				}
			} else { 
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
