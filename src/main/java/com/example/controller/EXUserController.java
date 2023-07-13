package com.example.controller;

import java.text.SimpleDateFormat; 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.example.entity.EXUser;
import com.example.entity.LoginRequest;
import com.example.entity.Partnership;
import com.example.entity.ResponseBean;
import com.example.entity.WebsiteBean;
import com.example.repository.EXUserRepository;
import com.example.repository.WebsiteBeanRepository;

@RestController
@RequestMapping("/exuser")
public class EXUserController {

	@Autowired
	private EXUserRepository userRepo;

	@Autowired
	private WebsiteBeanRepository webRepo;

	String regex = "^(?=.*[0-9])" + "(?=.*[a-z])(?=.*[A-Z])" + "(?=\\S+$).{8,15}$";

	Pattern p = Pattern.compile(regex);

	@Async("asyncExecutor")
	public CompletableFuture<HashMap<String, String>> validateUserConditions(EXUser parent) {
		HashMap<String, String> response = new HashMap<>();
		try {
			if (parent == null) {
				response.put("type", "error");
				response.put("message", "Invalid User Data");
				return CompletableFuture.completedFuture(response);
			}
			if (parent.getEmail() == null || !isValidEmailAddress(parent.getEmail())) {
				response.put("type", "error");
				response.put("message", "Invalid Email Address");
				return CompletableFuture.completedFuture(response);
			} else if (parent.getUserid().equalsIgnoreCase(null) || parent.getUserid().equalsIgnoreCase("")) {
				response.put("type", "error");
				response.put("message", "User Id Required");
				return CompletableFuture.completedFuture(response);
			} else if (parent.getUsername().equalsIgnoreCase("") || parent.getUsername().length() < 1) {
				response.put("type", "error");
				response.put("message", "UserName Must be grater than 1 Characters");
				return CompletableFuture.completedFuture(response);
			} else if (p.matcher(parent.getPassword()).matches() == false) {
				response.put("type", "error");
				response.put("message",
						"Password Must contains 1 Upper Case, 1 Lowe Case & 1 Numeric Value & in Between 8-15 Charachter");
				return CompletableFuture.completedFuture(response);
			} else if (parent.getMobileNumber().length() > 10) {
				response.put("type", "error");
				response.put("message", "Mobile Number Must Be Of 10 Digit or Balnk");
				return CompletableFuture.completedFuture(response);
			} else if (parent.getExposureLimit() == null) {
				response.put("type", "error");
				response.put("message", "Invalid Exposure Limit");
				return CompletableFuture.completedFuture(response);
//				}else if(parent.getc.equalsIgnoreCase(null) || userData.getString("userComm").equalsIgnoreCase("")){
//					response.put("type","error");
//					response.put("message","Invalid Commission");
//					return CompletableFuture.completedFuture(response);
			} else if (parent.getTimeZone().equalsIgnoreCase(null) || parent.getTimeZone().equalsIgnoreCase("")) {
				response.put("type", "error");
				response.put("message", "Invalid TimeZone");
				return CompletableFuture.completedFuture(response);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			response.put("type", "error");
			response.put("message", "Something Went Wrong");
			return CompletableFuture.completedFuture(response);
		}

		response.put("type", "success");
		response.put("message", "Pass");
		return CompletableFuture.completedFuture(response);
	}

	public boolean isValidEmailAddress(String email) {
		String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
		java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
		java.util.regex.Matcher m = p.matcher(email);
		return m.matches();
	}

//		 @ResponseBody
//		 @RequestMapping(value = "/validateUserCreation",method = RequestMethod.POST)
	@PostMapping("/validateUserCreation")
	public ResponseEntity<Object> validateUserCreation(@RequestBody EXUser requestData) {
//				HttpSession session = request.getSession(true);
		ResponseBean responseBean = new ResponseBean();
//				EXUser usersession = (EXUser) session.getAttribute("user");
//				JSONObject userData = new JSONObject(requestData);
		try {
			EXUser checkUser = userRepo.findByUserid(requestData.getUserid().toLowerCase());
			ArrayList isValidUser = new ArrayList<>();

			CompletableFuture<HashMap<String, String>> conditions = validateUserConditions(requestData);
			CompletableFuture.allOf(conditions).join();
			HashMap<String, String> conditionsReturn = new HashMap<>();
			try {
				conditionsReturn = conditions.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (conditionsReturn.containsKey("type") && conditionsReturn.get("type").equalsIgnoreCase("error")) {
				responseBean.setType(conditionsReturn.get("type"));
				responseBean.setMessage(conditionsReturn.get("message"));
				responseBean.setTitle("Error");
				return new ResponseEntity<Object>(responseBean, HttpStatus.ACCEPTED);
			}
			if (requestData.getUsertype() == 0) {
//							WebsiteBean webbean = new WebsiteBean();
//							WebsiteBean web = webRepo.findByid(webbean.getId());
//							if(web == null){
//								responseBean.setType("error");
//								responseBean.setMessage("Please Select a Valid Website");
//								responseBean.setTitle("Error");
//								return new ResponseEntity<Object>(responseBean,HttpStatus.ACCEPTED);
//							}

				requestData = saveSubAdmin(requestData);
				if (requestData.getUserid() != null) {
					userRepo.save(requestData);
//								web.setIsUsed(true);
//								if(web.getUsedBy().equalsIgnoreCase("-")){
//									web.setUsedBy(requestData.getUserid()+"("+requestData.getUsername()+")");
//								}else{
//									web.setUsedBy(web.getUsedBy()+", "+requestData.getUserid()+"("+requestData.getUsername()+")");
//								}
//								
//								webRepo.save(web);
					responseBean.setType("success");
					responseBean.setMessage("Success");
					responseBean.setTitle("Success");
					return new ResponseEntity<Object>(responseBean, HttpStatus.OK);
				}
			} else if (requestData.getUsertype() == 1) {
				requestData = saveMiniAdmin(requestData);
				if (requestData.getUserid() != null) {
					userRepo.save(requestData);
					responseBean.setType("success");
					responseBean.setMessage("Success");
					responseBean.setTitle("Success");
					return new ResponseEntity<Object>(responseBean, HttpStatus.OK);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		responseBean.setType("error");
		responseBean.setMessage("Not Authorized to Create this type of User");
		responseBean.setTitle("Error");
		return new ResponseEntity<Object>(responseBean, HttpStatus.ACCEPTED);

	}

	public EXUser saveSubAdmin(EXUser parent) {
		EXUser child = new EXUser();
		try {

			// child.setCreatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new
			// Date()), "GMT", "IST")));
			// child.setUpdatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new
			// Date()), "GMT", "IST")));
			child.setUsername(parent.getUsername());
			child.setUserid(parent.getUserid());
			child.setPassword(parent.getPassword());
			child.setUsertype(1);
			// child.setAccType(EXConstants.SUB_ADMIN);
			child.setAccountLock(false);
			child.setBetLock(false);
			child.setIsActive(true);
			child.setParentId(parent.getId());
			child.setParentName(parent.getUsername());
			child.setParentUserId(parent.getUserid());
			child.setAdminId(parent.getId());
			child.setAdminName(parent.getUsername());
			child.setAdminUserId(parent.getUserid());
			child.setSubadminId("0");
			child.setSubadminName("0");
			child.setSubadminUserId("0");
			child.setMiniadminId("0");
			child.setMiniadminName("0");
			child.setMiniadminUserId("0");
			child.setSupersuperId("0");
			child.setSupersuperName("0");
			child.setSupersuperUserId("0");
			child.setSupermasterId("0");
			child.setSupermasterName("0");
			child.setSupermasterUserId("0");
			child.setMasterId("0");
			child.setMasterName("0");
			child.setMasterUserId("0");
			child.setMyBalance(0.0);
			child.setFixLimit(0.0);
			child.setMyallPl(0.0);
			child.setMysportPl(0.0);
			child.setMycasinoPl(0.0);
			// child.setWebsiteId(userData.getString("websiteId"));
			// child.setWebsiteName(userData.getString("websiteName"));
			child.setSubChild("0");
			child.setMobileNumber(parent.getMobileNumber());
			child.setIspasswordChanged(false);
			child.setChildLiab(0.0);
			child.setLastName(parent.getLastName());
			child.setTimeZone(parent.getTimeZone());
			child.setEmail(parent.getEmail());
			child.setExposureLimit(0.0);

			Partnership childPartnership = new Partnership();

			childPartnership.setAdminSportPart(100.0);
			childPartnership.setSubadminSportPart(0.0);
			childPartnership.setMiniadminSportPart(0.0);
			childPartnership.setSupermasterSportPart(0.0);
			childPartnership.setSupermasterSportPart(0.0);
			childPartnership.setMasterSportPart(0.0);
			childPartnership.setUserComm(0.0);

			child.setPartnership(childPartnership);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return child;
	}

	public EXUser saveMiniAdmin(EXUser parent) {
		EXUser child = new EXUser();
		try {
			// child.setCreatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new
			// Date()), "GMT", "IST")));
			// child.setUpdatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new
			// Date()), "GMT", "IST")));
			child.setUsername(parent.getUsername());
			child.setUserid(parent.getUserid());
			child.setPassword(parent.getPassword());
			child.setUsertype(2);
			child.setChildLiab(0.0);
			// child.setAccType(EXConstants.MINI_ADMIN);
			child.setAccountLock(parent.getAccountLock());
			child.setBetLock(parent.getBetLock());
			child.setIsActive(parent.getIsActive());
			child.setAdminId(parent.getAdminId());
			child.setAdminName(parent.getAdminName());
			child.setAdminUserId(parent.getAdminUserId());
			child.setSubadminId(parent.getId());
			child.setSubadminName(parent.getUsername());
			child.setSubadminUserId(parent.getUserid());

			child.setParentId(parent.getId());
			child.setParentName(parent.getUsername());
			child.setParentUserId(parent.getUserid());

			child.setMiniadminId("0");
			child.setMiniadminName("0");
			child.setMiniadminUserId("0");
			child.setSupersuperId("0");
			child.setSupersuperName("0");
			child.setSupersuperUserId("0");
			child.setSupermasterId("0");
			child.setSupermasterName("0");
			child.setSupermasterUserId("0");
			child.setMasterId("0");
			child.setMasterName("0");
			child.setMasterUserId("0");
			child.setMyBalance(0.0);
			child.setFixLimit(0.0);
			child.setMyallPl(0.0);
			child.setMysportPl(0.0);
			child.setMycasinoPl(0.0);
			child.setWebsiteId(parent.getWebsiteId());
			child.setWebsiteName(parent.getWebsiteName());
			child.setSubChild("0");
			child.setMobileNumber(parent.getMobileNumber());
			child.setIspasswordChanged(false);
			child.setLastName(parent.getLastName());
			child.setTimeZone(parent.getTimeZone());
			child.setEmail(parent.getEmail());
			child.setExposureLimit(0.0);

			Partnership childPartnership = new Partnership();

			childPartnership.setAdminSportPart(100.0);
			childPartnership.setSubadminSportPart(0.0);
			childPartnership.setMiniadminSportPart(0.0);
			childPartnership.setSupermasterSportPart(0.0);
			childPartnership.setSupermasterSportPart(0.0);
			childPartnership.setMasterSportPart(0.0);
			childPartnership.setUserComm(0.0);

			child.setPartnership(childPartnership);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return child;
	}

	@PostMapping("/managementHome")
	public String managementHome(@RequestBody EXUser login) {

		// String host = req.getHeader("host");
		// ModelAndView model = new ModelAndView();
		// HttpSession session = request.getSession(true);
		// EXUser usersession =(EXUser) session.getAttribute("user");
		// EXUser user = null;
		// session.removeAttribute("userid");
		// FirebaseDatabase firebaseDatabase = null;
		// Calendar calendar = new GregorianCalendar();
		// TimeZone timeZone = calendar.getTimeZone();
		ResponseBean rbean = new ResponseBean();
		// String ipaddress = login.getAdminId();
		// Boolean isLoginValid = false;
		if (login.getUserid() != null && login.getPassword() != null) {
			Object user = userRepo.findByUseridAndPasswordAndIsActive(login.getUserid().toLowerCase(),
					login.getPassword(), true);
		} else {
			// rbean.setMessage("Please Fill All The Credential");
			// rbean.setType("error");
			// rbean.setTitle("Oops...");
			// model.addObject("result", rbean);
			// if(req.getHeader("User-Agent").contains("Mobile")){
			// model.setViewName("AMobilelogin");
			// }else{
			// model.setViewName("Alogin");
			// }
			return " !!";
		}
		SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat loginFormater = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		StringBuilder sb = new StringBuilder();

		// if(user!=null)
		// {
		// String userString = new Gson().toJson(user);
		// JSONObject jo = new JSONObject();
		// try {
		// jo = new JSONObject(userString);
		// } catch (JSONException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// if(usersession != null){
		// if(!usersession.getUserid().equalsIgnoreCase(user.getUserid())){
		// rbean.setMessage("Someone Is Already Loggedin");
		// rbean.setType("error");
		// rbean.setTitle("Oops...");
		// model.addObject("result", rbean);
		// if(req.getHeader("User-Agent").contains("Mobile")){
		// model.setViewName("AMobilelogin");
		// }else{
		// model.setViewName("Alogin");
		// }
		// return model;
		// }
		// }
		// if(user.getUsertype() == 6){
		// rbean.setMessage("Not Allowed to login");
		// rbean.setType("error");
		// rbean.setTitle("Oops...");
		// model.addObject("result", rbean);
		// if(req.getHeader("User-Agent").contains("Mobile")){
		// model.setViewName("AMobilelogin");
		// }else{
		// model.setViewName("Alogin");
		// }
		// }
		// else
		// {
		// if(user.getAccountLock()){
		// rbean.setMessage("Account Locked Please contact to Admin!");
		// rbean.setType("error");
		// rbean.setTitle("Oops...");
		// model.addObject("result", rbean);
		// if(req.getHeader("User-Agent").contains("Mobile")){
		// model.setViewName("AMobilelogin");
		// }else{
		// model.setViewName("Alogin");
		// }
		// }else{
		// try {
		//
		// firebaseDatabase = FirebaseDatabase.getInstance();
		//
		// } catch (Exception ex) {
		// ex.printStackTrace();
		// }
		//
		//
		// session.setAttribute("user",user);
		// session.setAttribute("userJson",jo);
		// model.setViewName("Adminhome");
		// UserIp userip = new UserIp();
		// if(session.getAttribute("user")!=null){
		// DatabaseReference databaseReference =
		// firebaseDatabase.getReference(EXConstants.Login_Point+""+user.getUserid());
		// databaseReference.child("sessionid").setValueAsync(session.getId());
		// databaseReference.child("ipAddress").setValueAsync(ipDao.getClientIp(request));
		// if(userIpRepo.findByuserid(user.getId())!=null){
		// userip = userIpRepo.findByuserid(user.getId());
		//
		// userip.setIpdetail(session.getId());
		// userip.setIpaddress(ipDao.getClientIp(request));
		//
		// userip.setLoggedin(true);
		// userip.setLastlogin(dtUtil.convTimeZone2(dateFormater.format(new Date()),
		// timeZone.getID(), "GMT"));
		// }else{
		// userip.setIpdetail(session.getId());
		// userip.setIpaddress(ipDao.getClientIp(request));
		// userip.setLoggedin(false);
		// userip.setUserid(user.getId());
		// userip.setLastlogin(dtUtil.convTimeZone2(dateFormater.format(new Date()),
		// timeZone.getID(), "GMT"));
		// }
		// UserActivityLog activityLog = new UserActivityLog();
		// activityLog.setActivityType(EXConstants.Login_Log);
		// activityLog.setIpaddress(ipDao.getClientIp(request));
		// activityLog.setUserid(user.getUserid());
		// activityLog.setCreatedOn(new Date());
		// activityLog.setNarration("-");
		// activityLog.setCity("-");
		// activityLog.setCountry("-");
		// activityLog.setActivityOn(activityFormater.format(new Date()));
		// if(request.getHeader("User-Agent").contains("Mobi")) {
		// activityLog.setIsMobile(true);
		// }else {
		// activityLog.setIsMobile(false);
		// }
		// userActivityRepo.save(activityLog);
		// if(userIpRepo.save(userip)==null){
		// session.removeAttribute("user");
		// rbean.setMessage("Something Went Wrong");
		// rbean.setType("error");
		// rbean.setTitle("Oops...");
		// model.addObject("result", rbean);
		// if(req.getHeader("User-Agent").contains("Mobile")){
		// model.setViewName("AMobilelogin");
		// }else{
		// model.setViewName("Alogin");
		// }
		// return model;
		// }
		// }
		// }
		//
		// }
		// }
		// else
		// {
		// rbean.setMessage("Invalid Login id or password!");
		// rbean.setType("error");
		// rbean.setTitle("Oops...");
		// model.addObject("result", rbean);
		// if(req.getHeader("User-Agent").contains("Mobile")){
		// model.setViewName("AMobilelogin");
		// }else{
		// model.setViewName("Alogin");
		// }
		// }

		return "Login Success!!!";
	}

	@PostMapping("/website")
	public ResponseEntity<ResponseBean> saveWebsite(@RequestBody WebsiteBean website) {
		String name = website.getName();
		if (webRepo.findByName(name) != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(new ResponseBean("error", "Website already exist!!", "WebSiteBean"));
		} else {
			webRepo.save(website);
		}
		return ResponseEntity.ok(new ResponseBean("Success", "Website Created Successfully!!", "WebSiteBean"));
	}

	@GetMapping("/allWebsite")
	public List<WebsiteBean> listOfWebsite() {
		List<WebsiteBean> findAll = webRepo.findAll();
		return findAll;
	}
	
	@PostMapping("/login")
	public ResponseEntity<ResponseBean> login(@RequestBody LoginRequest request) {

		if (request.getUserid() == null || request.getPassword() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseBean("error","UserId and password required","Management login"));
		}

		EXUser exUser = userRepo.findById(request.getUserid()).get();
		if (exUser != null && exUser.getPassword().equals(request.getPassword())) {
			return ResponseEntity.ok(new ResponseBean("Success","Login successful!","Management login"));
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseBean("error","Invalid userid or password.","Management login"));
		}
	}

}
