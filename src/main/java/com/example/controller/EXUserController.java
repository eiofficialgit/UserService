package com.example.controller;


import java.util.ArrayList; 
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.entity.EXUser;
import com.example.entity.Partnership;
import com.example.entity.ResponseBean;
import com.example.entity.UserStake;
import com.example.entity.WebsiteBean;
import com.example.repository.Authenticaterepo;
import com.example.repository.EXUserRepository;
import com.example.repository.WebsiteBeanRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/exuser")
@CrossOrigin("*")
public class EXUserController {

	@Autowired
	private EXUserRepository userRepo;

	@Autowired
	private Authenticaterepo authenticaterepo;
	
	@Autowired
	private WebsiteBeanRepository webRepo;
	
	@Autowired
	private HttpSession httpSession;

	@Autowired
	private RestTemplate restTemplate;
	
	

	String regex = "^(?=.*[0-9])" + "(?=.*[a-z])(?=.*[A-Z])" + "(?=\\S+$).{8,15}$";

	Pattern p = Pattern.compile(regex);

	@Async("asyncExecutor")
	public CompletableFuture<HashMap<String, String>> validateUserConditions(EXUser parent) {
		HashMap<String, String> response = new HashMap<>();
		String decryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/decode?decode="+parent.getPassword(),String.class);
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
			} else if (parent.getWebsitename().equalsIgnoreCase("") || parent.getWebsitename().length() < 1) {
				response.put("type", "error");
				response.put("message", "UserName Must be grater than 1 Characters");
				return CompletableFuture.completedFuture(response);
			} else if (p.matcher(decryptPassword).matches() == false) {
				response.put("type", "error");
				response.put("message",
						"Password Must contains 1 Upper Case, 1 Lowe Case & 1 Numeric Value & in Between 10-15 Charachter");
				return CompletableFuture.completedFuture(response);
			} else if (parent.getMobileNumber() == null || parent.getMobileNumber().length() > 10) {
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
			}else if (parent.getFirstName() == null || parent.getFirstName().isEmpty()) {
					response.put("type", "error");
					response.put("message", "Enter FirstName");
				} else if (parent.getLastName() == null || parent.getLastName().isEmpty()) {
					response.put("type", "error");
					response.put("message", "Enter LastName");
			} else if (parent.getTimeZone().equalsIgnoreCase(null) || parent.getTimeZone().equalsIgnoreCase("")) {
				response.put("type", "error");
				response.put("message", "Invalid TimeZone");
				return CompletableFuture.completedFuture(response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.put("type", "error");
			response.put("message", "Something Went Wrong !");
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
		
		
		EXUser requestData1 = (EXUser) httpSession.getAttribute("EXUser");
		
		
		
		try {
			EXUser checkUser = userRepo.findByUserid(((EXUser) requestData1).getUserid().toLowerCase());
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
			if (((EXUser) requestData1).getUsertype() == 0) {
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
			} else if (((EXUser) requestData1).getUsertype() == 1) {
				requestData = saveMiniAdmin(requestData);
				if (requestData.getUserid() != null) {
					userRepo.save(requestData);
					responseBean.setType("success");
					responseBean.setMessage("Success");
					responseBean.setTitle("Success");
					return new ResponseEntity<Object>(responseBean, HttpStatus.OK);
				}
			}else if(((EXUser) requestData1).getUsertype() == 2){
				checkUser = saveSuperSuper(requestData);
				if(checkUser.getUserid()!=null){
					userRepo.save(checkUser);
					responseBean.setType("success");
					responseBean.setMessage("Success");
					responseBean.setTitle("Success");
					return new ResponseEntity<Object>(responseBean,HttpStatus.OK);
				}
			}else if(((EXUser) requestData1).getUsertype() == 3){
				checkUser = saveSuperMaster(requestData);
				if(checkUser.getUserid()!=null){
					userRepo.save(checkUser);
					responseBean.setType("success");
					responseBean.setMessage("Success");
					responseBean.setTitle("Success");
					return new ResponseEntity<Object>(responseBean,HttpStatus.OK);
				}
			}else if(((EXUser) requestData1).getUsertype() == 4){
				checkUser = saveMaster(requestData);
				if(checkUser.getUserid()!=null){
					userRepo.save(checkUser);
					responseBean.setType("success");
					responseBean.setMessage("Success");
					responseBean.setTitle("Success");
					return new ResponseEntity<Object>(responseBean,HttpStatus.OK);
				}
			}else if(((EXUser) requestData1).getUsertype() == 5){
				checkUser = saveUser(requestData);
				if(checkUser.getUserid()!=null){
					userRepo.save(checkUser);
					responseBean.setType("success");
					responseBean.setMessage("Success");
					responseBean.setTitle("Success");
					return new ResponseEntity<Object>(responseBean,HttpStatus.OK);
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

	public EXUser saveSubAdmin(EXUser user) throws Exception {
		EXUser parent = (EXUser) httpSession.getAttribute("EXUser");
		
		EXUser child = new EXUser();
		String decryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/decode?decode="+user.getPassword(),String.class);
		String encryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/encode?encode="+decryptPassword,String.class);
		child.setPassword(encryptPassword);
		try {

			// child.setCreatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new
			// Date()), "GMT", "IST")));
			// child.setUpdatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new
			// Date()), "GMT", "IST")));
			child.setWebsitename(user.getWebsitename());
			child.setUserid(user.getUserid());
			
			child.setUsertype(1);
			// child.setAccType(EXConstants.SUB_ADMIN);
			child.setAccountLock(false);
			child.setBetLock(false);
			child.setIsActive(true);
			child.setParentId(parent.getId());
			child.setParentName(parent.getWebsitename());
			child.setParentUserId(parent.getUserid());
			child.setAdminId(parent.getId());
			child.setAdminName(parent.getWebsitename());
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
			child.setMobileNumber(user.getMobileNumber());
			child.setIspasswordChanged(false);
			child.setChildLiab(0.0);
			child.setLastName(user.getLastName());
			child.setTimeZone(user.getTimeZone());
			child.setEmail(user.getEmail());
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

	public EXUser saveMiniAdmin(EXUser user) {
		
		EXUser parent = (EXUser) httpSession.getAttribute("EXUser");
		EXUser child = new EXUser();
		String decryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/decode?decode="+user.getPassword(),String.class);
		String encryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/encode?encode="+decryptPassword,String.class);
		child.setPassword(encryptPassword);
		try {
			// child.setCreatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new
			// Date()), "GMT", "IST")));
			// child.setUpdatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new
			// Date()), "GMT", "IST")));
			child.setWebsitename(user.getWebsitename());
			child.setUserid(user.getUserid());
			
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
			child.setSubadminName(parent.getWebsitename());
			child.setSubadminUserId(parent.getUserid());

			child.setParentId(parent.getId());
			child.setParentName(parent.getWebsitename());
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
//			child.setWebsiteId(parent.getWebsiteId());
//			child.setWebsiteName(parent.getWebsiteName());
			child.setSubChild("0");
			child.setMobileNumber(user.getMobileNumber());
			child.setIspasswordChanged(false);
			child.setLastName(user.getLastName());
			child.setTimeZone(user.getTimeZone());
			child.setEmail(user.getEmail());
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
	
	public EXUser saveSuperSuper(EXUser user){
		EXUser parent = (EXUser) httpSession.getAttribute("EXUser");
		EXUser child = new EXUser();
		String decryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/decode?decode="+user.getPassword(),String.class);
		String encryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/encode?encode="+decryptPassword,String.class);
		child.setPassword(encryptPassword);
		try{
//			child.setCreatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new Date()), "GMT", "IST")));
//			child.setUpdatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new Date()), "GMT", "IST")));
			child.setWebsitename(user.getWebsitename());
			child.setUserid(user.getUserid());
			
			child.setUsertype(3);
			child.setChildLiab(0.0);
//			child.setAccType(EXConstants.SUPER_SUPER);
			child.setAccountLock(parent.getAccountLock());
			child.setBetLock(parent.getBetLock());
			child.setIsActive(parent.getIsActive());
			child.setSubadminId(parent.getSubadminId());
			child.setSubadminName(parent.getSubadminName());
			child.setSubadminUserId(parent.getSubadminUserId());
			child.setAdminId(parent.getAdminId());
			child.setAdminName(parent.getAdminName());
			child.setAdminUserId(parent.getAdminUserId());
			child.setMiniadminId(parent.getId());
			child.setMiniadminName(parent.getWebsitename());
			child.setMiniadminUserId(parent.getUserid());
			child.setSupersuperId("0");
			child.setSupersuperName("0");
			child.setSupersuperUserId("0");
			child.setSupermasterId("0");
			child.setSupermasterName("0");
			child.setSupermasterUserId("0");
			child.setMasterId("0");
			child.setMasterName("0");
			child.setMasterUserId("0");

			
			child.setParentId(parent.getId());
			child.setParentName(parent.getWebsitename());
			child.setParentUserId(parent.getUserid());
			child.setMyBalance(0.0);
			child.setFixLimit(0.0);
			child.setMyallPl(0.0);
			child.setMysportPl(0.0);
			child.setMycasinoPl(0.0);
//			child.setWebsiteId(parent.getWebsiteId());
//			child.setWebsiteName(parent.getWebsiteName());
			child.setSubChild("0");
			child.setMobileNumber(user.getMobileNumber());
			child.setIspasswordChanged(false);
			child.setLastName(user.getLastName());
			child.setTimeZone(user.getTimeZone());
			child.setEmail(user.getEmail());
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
			
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		
		return child;
	}
	
	
	public EXUser saveSuperMaster(EXUser user){
		EXUser parent = (EXUser) httpSession.getAttribute("EXUser");
		EXUser child = new EXUser();
		String decryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/decode?decode="+user.getPassword(),String.class);
		String encryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/encode?encode="+decryptPassword,String.class);
		child.setPassword(encryptPassword);
		try{
//			child.setCreatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new Date()), "GMT", "IST")));
//			child.setUpdatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new Date()), "GMT", "IST")));
			child.setWebsitename(user.getWebsitename());
			child.setUserid(user.getUserid());
			
			child.setUsertype(4);
			child.setChildLiab(0.0);
//			child.setAccType(EXConstants.SUPER_MASTER);
			child.setAccountLock(parent.getAccountLock());
			child.setBetLock(parent.getBetLock());
			child.setIsActive(parent.getIsActive());
			child.setSubadminId(parent.getSubadminId());
			child.setSubadminName(parent.getSubadminName());
			child.setSubadminUserId(parent.getSubadminUserId());
			child.setAdminId(parent.getAdminId());
			child.setAdminName(parent.getAdminName());
			child.setAdminUserId(parent.getAdminUserId());
			child.setMiniadminId(parent.getMiniadminId());
			child.setMiniadminName(parent.getMiniadminName());
			child.setMiniadminUserId(parent.getMiniadminUserId());
			child.setSupersuperId(parent.getId());
			child.setSupersuperName(parent.getWebsitename());
			child.setSupersuperUserId(parent.getUserid());
			child.setSupermasterId("0");
			child.setSupermasterName("0");
			child.setSupermasterUserId("0");
			child.setMasterId("0");
			child.setMasterName("0");
			child.setMasterUserId("0");

			child.setParentId(parent.getId());
			child.setParentName(parent.getWebsitename());
			child.setParentUserId(parent.getUserid());
			
			
			child.setMyBalance(0.0);
			child.setFixLimit(0.0);
			child.setMyallPl(0.0);
			child.setMysportPl(0.0);
			child.setMycasinoPl(0.0);
//			child.setWebsiteId(parent.getWebsiteId());
//			child.setWebsiteName(parent.getWebsiteName());
			child.setSubChild("0");
			child.setMobileNumber(user.getMobileNumber());
			child.setIspasswordChanged(false);
			child.setLastName(user.getLastName());
			child.setTimeZone(user.getTimeZone());
			child.setEmail(user.getEmail());
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
			
			
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		
		return child;
	}
	
	
	public EXUser saveMaster(EXUser user){
		EXUser parent = (EXUser) httpSession.getAttribute("EXUser");
		EXUser child = new EXUser();
		String decryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/decode?decode="+user.getPassword(),String.class);
		String encryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/encode?encode="+decryptPassword,String.class);
		child.setPassword(encryptPassword);
		try{
//			child.setCreatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new Date()), "GMT", "IST")));
//			child.setUpdatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new Date()), "GMT", "IST")));
			child.setWebsitename(user.getWebsitename());
			child.setUserid(user.getUserid());
			
			child.setUsertype(5);
			child.setChildLiab(0.0);
//			child.setAccType(EXConstants.MASTER);
			child.setAccountLock(parent.getAccountLock());
			child.setBetLock(parent.getBetLock());
			child.setIsActive(parent.getIsActive());
			child.setSubadminId(parent.getSubadminId());
			child.setSubadminName(parent.getSubadminName());
			child.setSubadminUserId(parent.getSubadminUserId());
			child.setAdminId(parent.getAdminId());
			child.setAdminName(parent.getAdminName());
			child.setAdminUserId(parent.getAdminUserId());
			child.setMiniadminId(parent.getMiniadminId());
			child.setMiniadminName(parent.getMiniadminName());
			child.setMiniadminUserId(parent.getMiniadminUserId());
			child.setSupersuperId(parent.getSupersuperId());
			child.setSupersuperName(parent.getSupersuperName());
			child.setSupersuperUserId(parent.getSupersuperUserId());
			child.setSupermasterId(parent.getId());
			child.setSupermasterName(parent.getWebsitename());
			child.setSupermasterUserId(parent.getUserid());
			child.setMasterId("0");
			child.setMasterName("0");
			child.setMasterUserId("0");
			
			child.setParentId(parent.getId());
			child.setParentName(parent.getWebsitename());
			child.setParentUserId(parent.getUserid());
			
			
			child.setMyBalance(0.0);
			child.setFixLimit(0.0);
			child.setMyallPl(0.0);
			child.setMysportPl(0.0);
			child.setMycasinoPl(0.0);
//			child.setWebsiteId(parent.getWebsiteId());
//			child.setWebsiteName(parent.getWebsiteName());
			child.setSubChild("0");
			child.setMobileNumber(user.getMobileNumber());
			child.setIspasswordChanged(false);
			child.setLastName(user.getLastName());
			child.setTimeZone(user.getTimeZone());
			child.setEmail(user.getEmail());
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
			
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		
		return child;
	}
	
	
	public EXUser saveUser(EXUser user){
		EXUser parent = (EXUser) httpSession.getAttribute("EXUser");
		EXUser child = new EXUser();
		String decryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/decode?decode="+user.getPassword(),String.class);
		String encryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/encode?encode="+decryptPassword,String.class);
		child.setPassword(encryptPassword);
		try{
//			child.setCreatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new Date()), "GMT", "IST")));
//			child.setUpdatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new Date()), "GMT", "IST")));
			child.setWebsitename(user.getWebsitename());
			child.setUserid(user.getUserid());
			
			child.setUsertype(6);
			child.setChildLiab(0.0);
//			child.setAccType(EXConstants.USER);
			child.setAccountLock(parent.getAccountLock());
			child.setBetLock(parent.getBetLock());
			child.setIsActive(parent.getIsActive());
			child.setSubadminId(parent.getSubadminId());
			child.setSubadminName(parent.getSubadminName());
			child.setSubadminUserId(parent.getSubadminUserId());
			child.setAdminId(parent.getAdminId());
			child.setAdminName(parent.getAdminName());
			child.setAdminUserId(parent.getAdminUserId());
			child.setMiniadminId(parent.getMiniadminId());
			child.setMiniadminName(parent.getMiniadminName());
			child.setMiniadminUserId(parent.getMiniadminUserId());
			child.setSupersuperId(parent.getSupersuperId());
			child.setSupersuperName(parent.getSupersuperName());
			child.setSupersuperUserId(parent.getSupersuperUserId());
			child.setSupermasterId(parent.getSupermasterId());
			child.setSupermasterName(parent.getSupermasterName());
			child.setSupermasterUserId(parent.getSupermasterUserId());
			child.setMasterId(parent.getId());
			child.setMasterName(parent.getWebsitename());
			child.setMasterUserId(parent.getUserid());
			
			child.setParentId(parent.getId());
			child.setParentName(parent.getWebsitename());
			child.setParentUserId(parent.getUserid());
			
			
			child.setMyBalance(0.0);
			child.setFixLimit(0.0);
			child.setMyallPl(0.0);
			child.setMysportPl(0.0);
			child.setMycasinoPl(0.0);
//			child.setWebsiteId(parent.getWebsiteId());
//			child.setWebsiteName(parent.getWebsiteName());
			child.setSubChild("0");
			child.setMobileNumber(user.getMobileNumber());
			child.setIspasswordChanged(false);
			child.setLastName(user.getLastName());
			child.setTimeZone(user.getTimeZone());
			child.setEmail(user.getEmail());
//			child.setExposureLimit(parent.getExposureLimit());
		
			UserStake stake = new UserStake();
			stake.setStake1(1000);
			stake.setStakename1("1000");
			stake.setStake2(5000);
			stake.setStakename2("5000");
			stake.setStake3(10000);
			stake.setStakename3("10000");
			stake.setStake4(25000);
			stake.setStakename4("25000");
			stake.setStake5(50000);
			stake.setStakename5("50000");
			stake.setStake6(100000);
			stake.setStakename6("100000");
			stake.setStake7(200000);
			stake.setStakename7("200000");
			stake.setStake8(500000);
			stake.setStakename8("500000");
			ArrayList<Integer> selectedStake = new ArrayList<>();
			for(int i =1;i<=6;i++){
				selectedStake.add(i);
			}
			stake.setSelectedStakes(selectedStake);
			
			Partnership childPartnership = new Partnership();
			
			childPartnership.setAdminSportPart(100.0);
			childPartnership.setSubadminSportPart(0.0);	
			childPartnership.setMiniadminSportPart(0.0);
			childPartnership.setSupermasterSportPart(0.0);
			childPartnership.setSupermasterSportPart(0.0);
			childPartnership.setMasterSportPart(0.0);
//			if(userData.getDouble("userComm")>2.0){
//				childPartnership.setUserComm(2.0);
//			}else{
//				childPartnership.setUserComm(userData.getDouble("userComm"));
//			}			
			
			child.setPartnership(childPartnership);
			child.setStake(stake);
			child.setRateDifference(3);
			child.setIsOneClickBet(false);
			child.setDefaultStake(0.0);
			child.setHighlightOdds(true);
			child.setAcceptAnyFancyOdds(false);
			child.setAcceptAnySportsBookOdds(false);
			child.setAcceptAnyBinaryOdds(false);
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		
		return child;
	}


	
	
	
	@PostMapping("/managementHome")
	public ResponseEntity<ResponseBean> managementHome(@RequestBody EXUser login) {
		

		
		
		EXUser users = authenticaterepo.findByUserid(login.getUserid());
		
		//user name null or wrong
		if(users==null) {
			ResponseBean reponsebean=ResponseBean.builder().title("ManagementHome").type("Error").message("Wrong UserId!!!").build();
		return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.UNAUTHORIZED);
		}
		
		//user password null or wrong
		if(!users.getPassword().equals(login.getPassword())) {
			ResponseBean reponsebean=ResponseBean.builder().title("ManagementHome").type("Error").message("Wrong password!!!").build();
		return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.UNAUTHORIZED);
		}
		
		httpSession.setAttribute("EXUser", users);

		ResponseBean reponsebean=ResponseBean.builder().title("ManagementHome").type("success").message(httpSession.getAttribute("EXUser")).build();
		return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.OK);
	}
	

		// String host = req.getHeader("host");
		// ModelAndView model = new ModelAndView();
		// HttpSession session = request.getSession(true);
		// EXUser usersession =(EXUser) session.getAttribute("user");
		// EXUser user = null;
		// session.removeAttribute("userid");
		// FirebaseDatabase firebaseDatabase = null;
		// Calendar calendar = new GregorianCalendar();
		// TimeZone timeZone = calendar.getTimeZone();
		//ResponseBean rbean = new ResponseBean();
		// String ipaddress = login.getAdminId();
		/*
		 * // Boolean isLoginValid = false; LoginRequest findByUsersId =
		 * userRepo.findByUsersId(login.getUserid());
		 * 
		 * if(findByUsersId==null) { return
		 * ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new
		 * ResponseBean("error","UserId and password required","Management login")); }
		 * 
		 * if(findByUsersId!=null &&
		 * findByUsersId.getPassword().equals(login.getPassword())) { return
		 * ResponseEntity.ok(new
		 * ResponseBean("Success","Login successful!","Management login")); }
		 * 
		 * //sucess ResponseBean reponsebean=ResponseBean.builder().message(sucess fully
		 * login).ty }
		 */
	
			// rbean.setMessage("Please Fill All The Credential");
			// rbean.setType("error");
			// rbean.setTitle("Oops...");
			// model.addObject("result", rbean);
			// if(req.getHeader("User-Agent").contains("Mobile")){
			// model.setViewName("AMobilelogin");
			// }else{
			// model.setViewName("Alogin");
			// }
			
//		SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		SimpleDateFormat loginFormater = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
//		StringBuilder sb = new StringBuilder();

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
	
	@PostMapping("/checkuser")
	public ResponseEntity<ResponseBean> checkuser(@RequestBody EXUser login) {
		EXUser users = userRepo.findByUserid(login.getUserid());
		if (users != null && users.getUserid().equals(login.getUserid())) {
			return ResponseEntity.ok(new ResponseBean("Error", "User already exist!!", "CheckUser"));
		} else {
			return ResponseEntity.ok(new ResponseBean("Success", "Valid User", "CheckUser"));
		}
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
	
	@GetMapping("/allchild")
	public List<EXUser> listUserType( ){
		EXUser parent = (EXUser) httpSession.getAttribute("EXUser");
		Integer usertype = parent.getUsertype()+1;
		List<EXUser> findByUsertype = userRepo.findByUsertype(usertype);
		return findByUsertype;
	}

	@GetMapping("/allchildwithpagination")
	public List<EXUser> listUserTypeWithPagination( @RequestParam("page") int page, @RequestParam("size") int size) {
	    EXUser parent = (EXUser) httpSession.getAttribute("EXUser");
	    Integer usertype = parent.getUsertype() + 1;
	    PageRequest pageable = PageRequest.of(page, size);
	    Page<EXUser> findByUsertype = userRepo.findByUsertype(usertype, pageable);
	    List<EXUser> users = findByUsertype.getContent();
	    return users;
	}
	
	@GetMapping("/{parentId}/{usertype}")
	public ResponseEntity<List<EXUser>> listOnHierarchy(@PathVariable String parentId, @PathVariable Integer usertype){
		EXUser parent = (EXUser) httpSession.getAttribute("EXUser");
		if(parent.getUsertype()<usertype) {
		List<EXUser> findByUsertype = userRepo.findByParentIdAndUsertype(parentId, usertype);
		return ResponseEntity.ok(findByUsertype);
		}else {
			return null ;
		}
	}
	
	@GetMapping("/logout")
	public ResponseEntity<ResponseBean> logout(HttpServletRequest request){
		HttpSession session = request.getSession(false);
		 if (session != null) {
	            session.invalidate();
	     }
		 return ResponseEntity.ok(new ResponseBean("success", "Logout Successfully!!", "ManagementHome"));
	}
	
}
