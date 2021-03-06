package com.hsk.services;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;

import com.hsk.beans.AulBean;
import com.hsk.beans.AuthBean;
import com.hsk.beans.ProBean;
import com.hsk.beans.ProInfoBean;
import com.hsk.beans.ProMembersBean;
import com.hsk.interfaces.ServiceRule;
import com.hsk.utils.Encryption;
import com.hsk.utils.ProjectUtils;

@Service
public class DashBoard implements ServiceRule {

	@Autowired
	private Encryption enc;

	@Autowired
	private SqlSessionTemplate session;

	@Autowired
	private ProjectUtils pu;

	public DashBoard() {
	}

	public void backController(int serviceCode, ModelAndView mav) {
		try {
			if (this.pu.getAttribute("accessInfo") != null) {
				switch (serviceCode) {
				case 0:
					this.entrance(mav); break;
				case 1:
					this.isMember(mav); break;
				default:
				}
			} else {
				mav.setViewName("index");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void backController(int serviceCode, Model model) {
		try {
			if (this.pu.getAttribute("accessInfo") != null) {
				switch (serviceCode) {
				case 0:	this.showProject(model); break;
				default:
				}
			} else {

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void entrance(ModelAndView mav) throws Exception {
		AuthBean auth = (AuthBean) this.pu.getAttribute("accessInfo");

		mav.addObject("receivedInfo", this.receivedInfo(auth));
		mav.addObject("sendInfo", this.sendInfo(auth));
		mav.setViewName("main");
	}

	private void showProject(Model model) {
		List<ProInfoBean> proList = new ArrayList<ProInfoBean>();
		ProInfoBean pi = (ProInfoBean) model.getAttribute("proInfoBean");
		proList = this.session.selectList("getMyProject", pi);
		try {
			if (proList.size()!=0) {
				proList.get(0).setType(true);
				for (ProInfoBean p : proList) {
					p.setDirector(this.enc.aesDecode(p.getDirector(), p.getDirCode()));
				}
			} else {
				proList = new ArrayList<ProInfoBean>();
				ProInfoBean p = new ProInfoBean();
				p.setType(false);
				proList.add(p);
			}
			model.addAttribute("ProjectList", proList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Transactional
	/* ???????????? ?????? -> ?????? ?????? ???????????? */
	private void isMember(ModelAndView mav) {
		// SimpleDateFormat stp = new SimpleDateFormat("yyyyMMddHHmmss");

		ProMembersBean pro = (ProMembersBean) mav.getModel().get("proMembersBean");
		AuthBean a = null;
		String page = "main";
		String message = "????????????";
		String code = "";

		try {
			a = ((AuthBean) this.pu.getAttribute("accessInfo"));

			code = (this.enc.aesDecode((pro.getProCode()), a.getPmbEmail())); // ???????????? ?????? ????????? ???????????? ????????? ??????
			pro.setProCode(code);
			pro.setPmbCode(a.getPmbCode());
			pro.setProPosition("MB");

			if (this.convertToBool(this.session.selectOne("isPro", pro))) {
				AulBean aul = new AulBean();
				aul = (AulBean) this.session.selectOne("isPrm", pro);

				/**/
				String strDate = aul.getInviteDate();
				SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				SimpleDateFormat newDtFormat = new SimpleDateFormat("yyyyMMddHHmmss");
				// String ????????? Date ???????????? ??????
				Date formatDate = dtFormat.parse(strDate);
				// Date????????? ????????? ????????? ????????? ???????????? ??????
				String strNewDtFormat = newDtFormat.format(formatDate);
				System.out.println("?????? ??? : " + strDate);
				System.out.println("?????? ??? : " + strNewDtFormat);

				aul.setInviteDate(strNewDtFormat);
				/**/

				System.out.println(aul.getInviteDate());
				if (aul != null) {
					this.session.update("updPrm", pro);
					aul.setAuthResult(pro.getProAccept() != null ? "AU" : "NA");
					this.session.update("updAul", aul);
					message = "????????????";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mav.addObject("receivedInfo", this.receivedInfo(a));
			mav.addObject("sendInfo", this.sendInfo(a));
			mav.addObject("message", message);

			mav.setViewName(page);
		}
	}

	private String receivedInfo(AuthBean auth) {
		StringBuffer sb = new StringBuffer();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		List<AulBean> aulList = new ArrayList<AulBean>();
		aulList = this.session.selectList("receivedInvitationInfo", auth);

		sb.append("<div id='rInfo'>");
		sb.append("<div class='notice title'>?????? ?????????</div>");
		sb.append("<div class='invitationList'>");
		sb.append(
				"<div class='member'><div class=\"items name\">?????????</div><div class=\"items invite\">????????????</div><div class=\"items expire\">????????????</div><div class=\"items accept\">??????</div></div>");
		for (AulBean a : aulList) {
			if (a.getAuthResult().equals("NA")) {
				boolean expired = Long.parseLong(a.getExpireDate().substring(0))
						- Long.parseLong(sdf.format(new Date())) >= 0 ? true : false;
				sb.append("<div class='member'>");
				try {
					sb.append("<div class='items' >" + this.enc.aesDecode(a.getSenderName(), a.getSender()) + "</div>");
				} catch (Exception e) {
					e.printStackTrace();
				}
				sb.append("<div class='items' >" + a.getInviteDate() + "</div>");
				sb.append("<div class='items' >" + a.getExpireDate() + "</div>");
				// sb.append("<div><input type='button' value='??????'
				// onclick='acceptCtl(\""+a.getInviteDate()+"\", \"AC\", "+expired+" )'
				// class='small-btn' /></div>");
				// sb.append("<div><input type='button' value='??????'
				// onclick='acceptCtl(\""+a.getInviteDate()+"\", \"RF\", "+expired+" )'
				// class='small-btn' /></div>");
				sb.append("<div><input type='button' value='??????' onclick='acceptCtl(\"" + a.getInviteDate()
						+ "\", \"AC\" )' class='small-btn' " + (expired ? "" : "disabled") + " /></div>");
				sb.append("<div><input type='button' value='??????' onclick='acceptCtl(\"" + a.getInviteDate()
						+ "\", \"RF\" )' class='small-btn' " + (expired ? "" : "disabled") + " /></div>");
				sb.append("</div>");
			}
		}
		sb.append("</div>");
		sb.append("</div>");
		return sb.toString();
	}

	private String sendInfo(AuthBean auth) {
		StringBuffer sb = new StringBuffer();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		List<AulBean> aulList = new ArrayList<AulBean>();
		aulList = this.session.selectList("sendInvitationInfo", auth);

		sb.append("<div id='sInfo'>");
		sb.append("<div class='notice title'>?????? ?????????</div>");
		sb.append("<div class='invitationList'>");
		sb.append(
				"<div class='member'><div class=\"items name\">?????????</div><div class=\"items invite\">????????????</div><div class=\"items expire\">????????????</div><div class=\"items accept\">??????</div></div>");
		for (AulBean a : aulList) {
			boolean expired = Long.parseLong(a.getExpireDate().substring(0))
					- Long.parseLong(sdf.format(new Date())) >= 0 ? true : false;
			sb.append("<div class='member'>");
			try {
				sb.append("<div class='items' >" + this.enc.aesDecode(a.getReceiverName(), a.getReceiver()) + "</div>");
			} catch (Exception e) {
				e.printStackTrace();
			}
			sb.append("<div class='items' >" + a.getInviteDate() + "</div>");
			sb.append("<div class='items' >" + a.getExpireDate() + "</div>");
			sb.append("<div class='items' >"
					+ ((expired || a.getAuthResult().equals("AU")) ? a.getAuthResultName() : "????????????") + "</div>");
			sb.append("</div>");
		}
		sb.append("</div>");
		sb.append("</div>");
		return sb.toString();
	}

	private boolean convertToBool(int value) {
		return value == 0 ? false : true;
	}

}
