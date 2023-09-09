package com.example.controller;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.entity.ActivityLog;
import com.example.entity.ActivityLogResponse;
import com.example.entity.ChangePassword;
import com.example.entity.CreditReferenceLog;
import com.example.entity.CreditReferenceLogResponse;
import com.example.entity.DecryptResponse;
import com.example.entity.DepositWithdraw;
import com.example.entity.EXUser;
import com.example.entity.EXUserResponse;
import com.example.entity.EncodedPayload;
import com.example.entity.HyperMessage;
import com.example.entity.ImportantMessage;
import com.example.entity.Partnership;
import com.example.entity.ResponseBean;
import com.example.entity.TransactionHistory;
import com.example.entity.TransactionHistoryResponse;
import com.example.entity.UserStake;
import com.example.entity.validationModel;
import com.example.entity.WebsiteBean;
import com.example.repository.ActivityLogRepo;
import com.example.repository.Authenticaterepo;
import com.example.repository.CreditReferenceLogRepo;
import com.example.repository.EXUserRepository;
import com.example.repository.HyperMessageRepo;
import com.example.repository.ImportantMessageRepo;
import com.example.repository.TransactionHistoryRepo;
import com.example.repository.WebsiteBeanRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@CrossOrigin("*")
@RequestMapping("/exuser")
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
	
	@Autowired
	private TransactionHistoryRepo transactionHistoryRepo;
	
	@Autowired
	private HttpServletRequest httpServletRequest;
	
	@Autowired
	private ActivityLogRepo activityLogRepo;
	
	@Autowired
	private ImportantMessageRepo messageRepo;
	
	@Autowired
	private HyperMessageRepo hyperMessageRepo;
	
	@Autowired
	private CreditReferenceLogRepo creditReferenceLogRepo;
	
	

	String regex = "^(?=.*[0-9])" + "(?=.*[a-z])(?=.*[A-Z])" + "(?=\\S+$).{8,15}$";

	Pattern p = Pattern.compile(regex);
	
	String regex1="^[1-9][0-9]{9}$";
	
	Pattern p1= Pattern.compile(regex1);

	@Async("asyncExecutor")
	public CompletableFuture<HashMap<String, String>> validateUserConditions(EXUser childData) {
		HashMap<String, String> response = new HashMap<>();
		try {
			if (childData.getUserName() == null || childData.getUserName().isEmpty() || childData.getUserName().length() < 1) {
				response.put("type", "error");
				response.put("message", "username Must be Required");
				return CompletableFuture.completedFuture(response);
			} else if (childData.getEmail() == null || !isValidEmailAddress(childData.getEmail())) {
				response.put("type", "error");
				response.put("message", "Invalid Email Address");
				return CompletableFuture.completedFuture(response);
			} else if (childData.getUserid().equalsIgnoreCase(null) || childData.getUserid().equalsIgnoreCase("")) {
				response.put("type", "error");
				response.put("message", "User Id Must be Required");
				return CompletableFuture.completedFuture(response);
			} else if (childData.getPassword() == null || p.matcher(childData.getPassword()).matches()== false) {
				response.put("type", "error");
				response.put("message", "Password Must contains 1 Upper Case, 1 Lower Case & 1 Numeric Value & in Between 8-15 Charachter");
				return CompletableFuture.completedFuture(response);
			} else if (childData.getFirstName() == null || childData.getFirstName().isEmpty()) {
				response.put("type", "error");
				response.put("message", "Enter FirstName");
				return CompletableFuture.completedFuture(response);
			} else if (childData.getLastName() == null || childData.getLastName().isEmpty()) {
				response.put("type", "error");
				response.put("message", "Enter LastName");
				return CompletableFuture.completedFuture(response);
			} else if (childData.getMobileNumber() == null || childData.getMobileNumber().length()>10 || childData.getMobileNumber().length()<10 || !isValidMobileNumber(childData.getMobileNumber())) {
					response.put("type", "error");
					response.put("message", "Mobile Number Must Be Of 10 Digit");
					return CompletableFuture.completedFuture(response);
			} else if (childData.getTimeZone().equalsIgnoreCase(null) || childData.getTimeZone().equalsIgnoreCase("")) {
				response.put("type", "error");
				response.put("message", "Invalid TimeZone");
				return CompletableFuture.completedFuture(response);
			
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.put("type", "error");
			response.put("message", "Something went wrong!!");	
			return CompletableFuture.completedFuture(response);
		}
		EXUser findByUserid = userRepo.findByUserid(childData.getUserid().toLowerCase());
		if(findByUserid==null) {
			return CompletableFuture.completedFuture(response);
		}else {
			response.put("type", "error");
			response.put("message", "User Id Exist!!!");
			return CompletableFuture.completedFuture(response);
		}

//		response.put("type", "success");
//		response.put("message", "Pass");
//		return CompletableFuture.completedFuture(response);
	}

	public boolean isValidEmailAddress(String email) {
		String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
		java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
		java.util.regex.Matcher m = p.matcher(email);
		return m.matches();
	}
	
	public boolean isValidMobileNumber(String mobileNumber) {
		String ePattern  = "^[1-9][0-9]{9}$";
		java.util.regex.Pattern p1 = java.util.regex.Pattern.compile(ePattern);
		java.util.regex.Matcher m1 = p1.matcher(mobileNumber);
		return m1.matches();
	}
	

	@PostMapping("/validateUserCreation")
	public ResponseEntity<Object> validateUserCreation(@RequestBody EncodedPayload payload) {			
		EXUser parentData = (EXUser) httpSession.getAttribute("EXUser");	
		ResponseBean responseBean = new ResponseBean();
		String decryptUrl = "http://ENCRYPTDECRYPT-MS/api/decryptPayload";
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    HttpEntity<EncodedPayload> requestEntity = new HttpEntity<>(payload, headers);
	    EXUser childData = restTemplate.postForObject(decryptUrl, requestEntity, EXUser.class);
		
		
		try {
			EXUser checkUser = userRepo.findByUserid(((EXUser) parentData).getUserid().toLowerCase());
			ArrayList isValidUser = new ArrayList<>();

			CompletableFuture<HashMap<String, String>> conditions = validateUserConditions(childData);
			CompletableFuture.allOf(conditions).join();
			HashMap<String, String> conditionsReturn = new HashMap<>();
			try {
				conditionsReturn = conditions.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (conditionsReturn.containsKey("type") && conditionsReturn.get("type").equalsIgnoreCase("error")) {
				responseBean.setData(conditionsReturn.get("type"));
				responseBean.setMessage(conditionsReturn.get("message"));
				responseBean.setStatus("Error");
				return new ResponseEntity<Object>(responseBean, HttpStatus.ACCEPTED);
			}
			if (((EXUser) parentData).getUsertype() == 0) {
							WebsiteBean web = webRepo.findByname(childData.getWebsiteName());
							if(web == null){
								responseBean.setData("error");
								responseBean.setMessage("Please Select a Valid Website");
								responseBean.setStatus("Error");
								return new ResponseEntity<Object>(responseBean,HttpStatus.ACCEPTED);
							}

				EXUser saveSubAdmin = saveSubAdmin(childData);
				if (saveSubAdmin.getUserid() != null) {
					String userid = saveSubAdmin.getUserid();
					userRepo.save(saveSubAdmin);
					if (web.getUsedBy() == null) {
	                    web.setUsedBy(new ArrayList<String>());
	                }
					web.setIsUsed(true);
					web.getUsedBy().add(userid);
					webRepo.save(web);
					responseBean.setData("success");
					responseBean.setMessage("Success");
					responseBean.setStatus("Success");
					return new ResponseEntity<Object>(responseBean, HttpStatus.OK);
				}
			} else if (((EXUser) parentData).getUsertype() == 1) {
				childData = saveMiniAdmin(childData);
				if (childData.getUserid() != null) {
					userRepo.save(childData);
					responseBean.setData("success");
					responseBean.setMessage("Success");
					responseBean.setStatus("Success");
					return new ResponseEntity<Object>(responseBean, HttpStatus.OK);
				}
			}else if(((EXUser) parentData).getUsertype() == 2){
				checkUser = saveSuperSuper(childData);
				if(checkUser.getUserid()!=null){
					userRepo.save(checkUser);
					responseBean.setData("success");
					responseBean.setMessage("Success");
					responseBean.setStatus("Success");
					return new ResponseEntity<Object>(responseBean,HttpStatus.OK);
				}
			}else if(((EXUser) parentData).getUsertype() == 3){
				checkUser = saveSuperMaster(childData);
				if(checkUser.getUserid()!=null){
					userRepo.save(checkUser);
					responseBean.setData("success");
					responseBean.setMessage("Success");
					responseBean.setStatus("Success");
					return new ResponseEntity<Object>(responseBean,HttpStatus.OK);
				}
			}else if(((EXUser) parentData).getUsertype() == 4){
				checkUser = saveMaster(childData);
				if(checkUser.getUserid()!=null){
					userRepo.save(checkUser);
					responseBean.setData("success");
					responseBean.setMessage("Success");
					responseBean.setStatus("Success");
					return new ResponseEntity<Object>(responseBean,HttpStatus.OK);
				}
			}else if(((EXUser) parentData).getUsertype() == 5){
				checkUser = saveUser(childData);
				if(checkUser.getUserid()!=null){
					userRepo.save(checkUser);
					responseBean.setData("success");
					responseBean.setMessage("Success");
					responseBean.setStatus("Success");
					return new ResponseEntity<Object>(responseBean,HttpStatus.OK);
				}
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		responseBean.setData("error");
		responseBean.setMessage("Not Authorized to Create this type of User");
		responseBean.setStatus("Error");
		return new ResponseEntity<Object>(responseBean, HttpStatus.ACCEPTED);

	}

	public EXUser saveSubAdmin(EXUser user) throws Exception {
		EXUser parent = (EXUser) httpSession.getAttribute("EXUser");
		
		EXUser child = new EXUser();
		String encryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/encode?encode="+user.getPassword(),String.class);
		child.setPassword(encryptPassword);
		WebsiteBean website = new WebsiteBean();
		try {

			// child.setCreatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new Date()), "GMT", "IST")));
			// child.setUpdatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new Date()), "GMT", "IST")));
			child.setUserName(user.getUserName());
			child.setUserid(user.getUserid());			
			child.setUsertype(1);
			// child.setAccType(EXConstants.SUB_ADMIN);
			child.setAccountLock(false);
			child.setBetLock(false);
			child.setIsActive(true);
			child.setParentId(parent.getId());
			child.setParentName(parent.getUserName());
			child.setParentUserId(parent.getUserid());
			child.setAdminId(parent.getId());
			child.setAdminName(parent.getUserName());
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
			child.setMyBalance(1000.0);
			child.setFixLimit(1000.0);
			child.setMyallPl(0.0);
			child.setMysportPl(0.0);
			child.setMycasinoPl(0.0);
			child.setWebsiteId(new WebsiteBean().getId());
			child.setWebsiteName(new WebsiteBean().getName());
			child.setSubChild("0");
			child.setMobileNumber(user.getMobileNumber());
			child.setIspasswordChanged(false);
			child.setChildLiab(0.0);
			child.setFirstName(user.getFirstName());
			child.setLastName(user.getLastName());
			child.setTimeZone(user.getTimeZone());
			child.setEmail(user.getEmail());
			child.setExposureLimit(1000.0);

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
		String encryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/encode?encode="+user.getPassword(),String.class);
		child.setPassword(encryptPassword);
		try {

			// child.setCreatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new
			// Date()), "GMT", "IST")));
			// child.setUpdatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new
			// Date()), "GMT", "IST")));
			child.setUserName(user.getUserName());
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
			child.setSubadminName(parent.getUserName());
			child.setSubadminUserId(parent.getUserid());

			child.setParentId(parent.getId());
			child.setParentName(parent.getUserName());
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
			child.setFirstName(user.getFirstName());
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
		String encryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/encode?encode="+user.getPassword(),String.class);
		child.setPassword(encryptPassword);
		try {

			// child.setCreatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new
			// Date()), "GMT", "IST")));
			// child.setUpdatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new
			// Date()), "GMT", "IST")));
			child.setUserName(user.getUserName());
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
			child.setMiniadminName(parent.getUserName());
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
			child.setParentName(parent.getUserName());
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
			child.setFirstName(user.getFirstName());
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
		String encryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/encode?encode="+user.getPassword(),String.class);
		child.setPassword(encryptPassword);
		try{
//			child.setCreatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new Date()), "GMT", "IST")));
//			child.setUpdatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new Date()), "GMT", "IST")));
			child.setUserName(user.getUserName());
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
			child.setSupersuperName(parent.getUserName());
			child.setSupersuperUserId(parent.getUserid());
			child.setSupermasterId("0");
			child.setSupermasterName("0");
			child.setSupermasterUserId("0");
			child.setMasterId("0");
			child.setMasterName("0");
			child.setMasterUserId("0");

			child.setParentId(parent.getId());
			child.setParentName(parent.getUserName());
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
			child.setFirstName(user.getFirstName());
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
		String encryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/encode?encode="+user.getPassword(),String.class);
		child.setPassword(encryptPassword);
		try{
//			child.setCreatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new Date()), "GMT", "IST")));
//			child.setUpdatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new Date()), "GMT", "IST")));
			child.setUserName(user.getUserName());
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
			child.setSupermasterName(parent.getUserName());
			child.setSupermasterUserId(parent.getUserid());
			child.setMasterId("0");
			child.setMasterName("0");
			child.setMasterUserId("0");
			
			child.setParentId(parent.getId());
			child.setParentName(parent.getUserName());
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
			child.setFirstName(user.getFirstName());
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
		String encryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/encode?encode="+user.getPassword(),String.class);
		child.setPassword(encryptPassword);
		try{
//			child.setCreatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new Date()), "GMT", "IST")));
//			child.setUpdatedOn(dateFormater.parse(dtUtil.convTimeZone2(dateFormater.format(new Date()), "GMT", "IST")));
			child.setUserName(user.getUserName());
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
			child.setMasterName(parent.getUserName());
			child.setMasterUserId(parent.getUserid());
			
			child.setParentId(parent.getId());
			child.setParentName(parent.getUserName());
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
			child.setFirstName(user.getFirstName());
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
	public ResponseEntity<ResponseBean> managementHome(@RequestBody EncodedPayload payload) {
		
		String decryptUrl = "http://ENCRYPTDECRYPT-MS/api/decryptPayload";
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    HttpEntity<EncodedPayload> requestEntity = new HttpEntity<>(payload, headers);
	    DecryptResponse decryptData = restTemplate.postForObject(decryptUrl, requestEntity, DecryptResponse.class);
	    String encryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/encode?encode="+decryptData.getPassword(),String.class);
		EXUser users = authenticaterepo.findByUserid(decryptData.getUserid());
		
		//user name null or wrong
		if(users==null) {
			ResponseBean reponsebean=ResponseBean.builder().data("ManagementHome").status("Error").message("Wrong UserId!!!").build();
		return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.UNAUTHORIZED);
		}
		
		//user password null or wrong
		if(!users.getPassword().equals(encryptPassword)) {
			ResponseBean reponsebean=ResponseBean.builder().data("ManagementHome").status("Error").message("Wrong password!!!").build();
		return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.UNAUTHORIZED);
		}
		
		if (!users.getIsActive()) {
            ResponseBean responseBean = ResponseBean.builder().data("ManagementHome").status("Error").message("Account Locked Please contact the Admin").build();
            return new ResponseEntity<>(responseBean, HttpStatus.UNAUTHORIZED);
        }
		
		httpSession.setAttribute("EXUser", users);
		
		ActivityLog log = new ActivityLog();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	    Date date = new Date();
	    log.setUserid(users.getUserid());
	    log.setDate_time(sdf.format(date));
	    log.setIpAddress(httpServletRequest.getRemoteAddr());
	    log.setLoginStatus("Login--");
	    activityLogRepo.save(log);
		
		String  encryptUrl = "http://ENCRYPTDECRYPT-MS/api/encryptPayload";
		HttpEntity<EXUser> userRequestEntity = new HttpEntity<>(users, headers);
		String encryptUserData = restTemplate.postForObject(encryptUrl, userRequestEntity, String.class);

		ResponseBean reponsebean=ResponseBean.builder().data(encryptUserData).status("success").message("User login Successfull!!").build();
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
	public ResponseEntity<ResponseBean> checkuser(@RequestBody EncodedPayload payload) {
		
		String decryptUrl = "http://ENCRYPTDECRYPT-MS/api/decryptPayload";
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    HttpEntity<EncodedPayload> requestEntity = new HttpEntity<>(payload, headers);
	    DecryptResponse decryptData = restTemplate.postForObject(decryptUrl, requestEntity, DecryptResponse.class);
	  
		EXUser users = authenticaterepo.findByUserid(decryptData.getUserid());
		if (users != null && users.getUserid().equals(decryptData.getUserid())) {
			ResponseBean reponsebean=ResponseBean.builder().data("CheckUser").status("Error").message("User already exist!!").build();
			return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.OK);
		} else {
			ResponseBean reponsebean=ResponseBean.builder().data("CheckUser").status("success").message("Valid User").build();
			return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.OK);
		}
	}
	
	
	
	@PostMapping("/addWebsite")
	public ResponseEntity<ResponseBean> saveWebsite(@RequestBody EncodedPayload payload) {
		
		String decryptUrl = "http://ENCRYPTDECRYPT-MS/api/decryptWebsiteBean";
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    HttpEntity<EncodedPayload> requestEntity = new HttpEntity<>(payload, headers);
	    WebsiteBean decryptData = restTemplate.postForObject(decryptUrl, requestEntity, WebsiteBean.class);
		
		String name = decryptData.getName();
		if (webRepo.findByName(name) != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(new ResponseBean("error", "Website already exist!!", "WebSiteBean"));
		} else {
			decryptData.setIsUsed(false);
			webRepo.save(decryptData);
		}
		return ResponseEntity.ok(new ResponseBean("Success", "Website Created Successfully!!", "WebSiteBean"));
	}
	
	

	
	@GetMapping("/allWebsite")
	public ResponseEntity<ResponseBean> listOfWebsite() {
		List<WebsiteBean> findAll = webRepo.findAll();
		String decryptUrl = "http://ENCRYPTDECRYPT-MS/api/encryptPayload";
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    EncodedPayload encodedPayload=new EncodedPayload();
	    Gson gson = new Gson();
		String data = gson.toJson(findAll);
		JsonArray jsonArray = new JsonParser().parse(data).getAsJsonArray();
		JSONObject jObj = new JSONObject();
		jObj.put("data", jsonArray);
	    encodedPayload.setPayload(jObj.toString());
	    HttpEntity<EncodedPayload> requestEntity = new HttpEntity<>(encodedPayload, headers);
	    String encryptwebsiteData = restTemplate.postForObject(decryptUrl, requestEntity, String.class);
	    ResponseBean reponsebean=ResponseBean.builder().data(encryptwebsiteData).status("success").message("All website fetch Successfull!!").build();
		return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.OK);
	}
	
	

	
	@GetMapping("/{parentId}/{usertype}")
	public ResponseEntity<ResponseBean> listOnHierarchy(@PathVariable String parentId, @PathVariable Integer usertype, @RequestParam("pageNumber") int pageNumber,@RequestParam("pageSize") int pageSize){
		
//		EXUser exUser = userRepo.findById(parentId).get();
		
//		if(exUser.getBetLock()==false) {
			EXUser parent = (EXUser) httpSession.getAttribute("EXUser");
		if(parent.getUsertype()<usertype) {
			Pageable pageable = PageRequest.of(pageNumber, pageSize);
			Page<EXUser> findByUsertype = userRepo.findByParentIdAndUsertype(parentId, usertype, pageable);
		    EXUserResponse response = new EXUserResponse();
		    List<EXUser> content = findByUsertype.getContent();
		    response.setContent(content);
		    response.setPageNumber(findByUsertype.getNumber());
		    response.setPageSize(findByUsertype.getSize());
		    response.setTotalElements(findByUsertype.getTotalElements());
		    response.setTotalPages(findByUsertype.getTotalPages());
		    response.setLastPage(findByUsertype.isLast());
		    String encryptUrl = "http://ENCRYPTDECRYPT-MS/api/encryptPayload";
		    HttpHeaders headers = new HttpHeaders();
		    headers.setContentType(MediaType.APPLICATION_JSON);
		    Gson gson = new Gson();
		    String data = gson.toJson(response);
		    EncodedPayload encodedPayload = new EncodedPayload();
		    encodedPayload.setPayload(data);
		    JsonObject jsonObject = new JsonParser().parse(data).getAsJsonObject();
			JSONObject jObj = new JSONObject();	
		    jObj.put("data", jsonObject);
		    response.setPayload(jObj.toString());
		    HttpEntity<EncodedPayload> requestEntity = new HttpEntity<>(encodedPayload, headers);
		    String encryptData = restTemplate.postForObject(encryptUrl, requestEntity, String.class);
		    ResponseBean responseBean = ResponseBean.builder().data(encryptData).status("success").message("All Childs fetch Successful!!").build();
		    return new ResponseEntity<>(responseBean, HttpStatus.OK);
		      }else {
			        ResponseBean responseBean = ResponseBean.builder().data("Downline List").status("Error").message("Something went wrong!!").build();
		            return new ResponseEntity<>(responseBean, HttpStatus.OK);
		}
//		}else {
//			ResponseBean responseBean = ResponseBean.builder().data("Downline List").status("Error").message("Account Suspended Please contact the Admin").build();
//		    return new ResponseEntity<>(responseBean, HttpStatus.OK);
//		}
	}
	
	
	@GetMapping("/search/{parentId}/{usertype}")
	public ResponseEntity<ResponseBean> searchWithPagination(@PathVariable String parentId, @PathVariable Integer usertype, @RequestParam("pageNumber") int pageNumber, @RequestParam("pageSize") int pageSize, @RequestParam("userid") String userid ) {

	    EXUser parent = (EXUser) httpSession.getAttribute("EXUser");

	    if (parent.getUsertype() < usertype) {
	        Pageable pageable = PageRequest.of(pageNumber, pageSize);

	        List<EXUser> findByUsertype = userRepo.findByParentIdAndUsertype(parentId, usertype);

	        if (userid != null && !userid.isEmpty()) {
	            findByUsertype = findByUsertype.stream()
	                    .filter(user -> user.getUserid().contains(userid))
	                    .collect(Collectors.toList());
	        }

	        Page<EXUser> paginatedUsers = getPage(findByUsertype, pageable);

	        EXUserResponse response = new EXUserResponse();
	        List<EXUser> content = paginatedUsers.getContent();
	        response.setContent(content);
	        response.setPageNumber(paginatedUsers.getNumber());
	        response.setPageSize(paginatedUsers.getSize());
	        response.setTotalElements(paginatedUsers.getTotalElements());
	        response.setTotalPages(paginatedUsers.getTotalPages());
	        response.setLastPage(paginatedUsers.isLast());

	        String encryptUrl = "http://ENCRYPTDECRYPT-MS/api/encryptPayload";
		    HttpHeaders headers = new HttpHeaders();
		    headers.setContentType(MediaType.APPLICATION_JSON);
		    Gson gson = new Gson();
		    String data = gson.toJson(response);
		    EncodedPayload encodedPayload = new EncodedPayload();
		    encodedPayload.setPayload(data);
		    JsonObject jsonObject = new JsonParser().parse(data).getAsJsonObject();
			JSONObject jObj = new JSONObject();	
		    jObj.put("data", jsonObject);
		    response.setPayload(jObj.toString());
		    HttpEntity<EncodedPayload> requestEntity = new HttpEntity<>(encodedPayload, headers);
		    String encryptData = restTemplate.postForObject(encryptUrl, requestEntity, String.class);

	        ResponseBean responseBean = ResponseBean.builder().data(encryptData).status("success").message("Filtered Users fetch Successful!!").build();

	        return new ResponseEntity<>(responseBean, HttpStatus.OK);
	    } else {
	        ResponseBean responseBean = ResponseBean.builder().data("Downline List").status("Error").message("Something went wrong!!").build();
	        return new ResponseEntity<>(responseBean, HttpStatus.OK);
	    }
	}

	private Page<EXUser> getPage(List<EXUser> content, Pageable pageable) {
	    int start = (int) pageable.getOffset();
	    int end = Math.min((start + pageable.getPageSize()), content.size());

	    return new PageImpl<>(content.subList(start, end), pageable, content.size());
	}
	
	
	@PostMapping("/action/{action}")
	public ResponseEntity<ResponseBean> action(@RequestBody EncodedPayload payload, @PathVariable String action ){
		
		EXUser parent = (EXUser) httpSession.getAttribute("EXUser");
		
		
		String decryptUrl = "http://ENCRYPTDECRYPT-MS/api/decryptPayload";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EncodedPayload> requestEntity = new HttpEntity<>(payload, headers);
        DecryptResponse decryptData = restTemplate.postForObject(decryptUrl, requestEntity, DecryptResponse.class);
        String encryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/encode?encode=" + decryptData.getPassword(), String.class);
	    EXUser user = authenticaterepo.findByUserid(decryptData.getUserid());
		
		if(parent.getPassword().equals(encryptPassword)) {
	    
		     if(action.equalsIgnoreCase("lock")) {
			    lockUserAndDescendants(user);
		      }
		
		      else if(action.equalsIgnoreCase("active")) {
			    activeUserAndDescendants(user);
		      }
		     
		      else if(action.equalsIgnoreCase("suspend")) {
		    	  suspendUserAndDescendants(user);
			  }
		
		     ResponseBean responseBean = ResponseBean.builder().data("UserAction").status("success").message("User and their child users status updated successfully").build();
	         return new ResponseEntity<>(responseBean, HttpStatus.OK);
	
	   }else {
		ResponseBean responseBean = ResponseBean.builder().data("UserAction").status("Error").message("Enter a valid Password!!").build();
	    return new ResponseEntity<>(responseBean, HttpStatus.OK);
	   }
	}
	
	private void lockUserAndDescendants(EXUser user) {
	    user.setIsActive(false);
	    user.setAccountLock(true);
	    user.setBetLock(false);
	    authenticaterepo.save(user);

	    List<EXUser> childUsers = userRepo.findByParentId(user.getId());
	    for (EXUser childUser : childUsers) {
	        lockUserAndDescendants(childUser);
	    }
	}
	
	private void activeUserAndDescendants(EXUser user) {
	    user.setIsActive(true);
	    user.setAccountLock(false);
	    user.setBetLock(false);
	    authenticaterepo.save(user);

	    List<EXUser> childUsers = userRepo.findByParentId(user.getId());
	    for (EXUser childUser : childUsers) {
	    	activeUserAndDescendants(childUser);
	    }
	}
	
	private void suspendUserAndDescendants(EXUser user) {
	    user.setIsActive(false);
	    user.setAccountLock(true);
	    user.setBetLock(true);
	    authenticaterepo.save(user);

	    List<EXUser> childUsers = userRepo.findByParentId(user.getId());
	    for (EXUser childUser : childUsers) {
	    	activeUserAndDescendants(childUser);
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
	
	
	
	@PostMapping("/creditReference")
	public ResponseEntity<ResponseBean> creditReference(@RequestBody EncodedPayload payload) {
		EXUser parent = (EXUser) httpSession.getAttribute("EXUser");
		String decryptUrl = "http://ENCRYPTDECRYPT-MS/api/decryptPayload";
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    HttpEntity<EncodedPayload> requestEntity = new HttpEntity<>(payload, headers);
	    EXUser user = restTemplate.postForObject(decryptUrl, requestEntity, EXUser.class);
	    String encryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/encode?encode="+user.getPassword(),String.class);
	    String userid = user.getUserid();
	    CreditReferenceLog creditReferenceLog = new CreditReferenceLog();
		
		EXUser currentUser = userRepo.findByUserid(userid.toLowerCase());
		if(user.getFixLimit()==null) {
			ResponseBean reponsebean=ResponseBean.builder().data("CreditReference").status("error").message("Please enter Credit Reference").build();
			return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.OK);
		}
		if(parent.getPassword().equals(encryptPassword)) {
			creditReferenceLog.setOldValue(currentUser.getFixLimit());
			currentUser.setFixLimit(user.getFixLimit());
			userRepo.save(currentUser);
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		    Date date = new Date();
			creditReferenceLog.setDate(sdf.format(date));
			creditReferenceLog.setNewValue(currentUser.getFixLimit());
			creditReferenceLog.setUserid(currentUser.getUserid());
			creditReferenceLogRepo.save(creditReferenceLog);
			ResponseBean reponsebean=ResponseBean.builder().data("CreditReference").status("success").message("Credit Reference updated Successfull!!").build();
			return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.OK);
		}else {
			ResponseBean reponsebean=ResponseBean.builder().data("CreditReference").status("Error").message("Wrong Password!!").build();
			return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.OK);
		}
		
	}
	
	@PostMapping("/creditReferenceLog")
	public ResponseEntity<ResponseBean> creditReferenceLog(@RequestBody EncodedPayload payload, @RequestParam("pageNumber") int pageNumber, @RequestParam("pageSize") int pageSize) {
		
		String decryptData = "http://ENCRYPTDECRYPT-MS/api/decryptPayload";
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    HttpEntity<EncodedPayload> requestEntity = new HttpEntity<>(payload, headers);
	    EXUser user = restTemplate.postForObject(decryptData, requestEntity, EXUser.class);
		
		String userid = user.getUserid();
		Pageable pageable = PageRequest.of(pageNumber, pageSize);
	    Page<CreditReferenceLog> creditReferenceList = creditReferenceLogRepo.findByuserid(userid, pageable);
	    CreditReferenceLogResponse creditReferenceLogResponse = new CreditReferenceLogResponse();
	    List<CreditReferenceLog> contents = creditReferenceList.getContent();
	    creditReferenceLogResponse.setContent(contents);
	    creditReferenceLogResponse.setPageNumber(creditReferenceList.getNumber());
	    creditReferenceLogResponse.setPageSize(creditReferenceList.getSize());
	    creditReferenceLogResponse.setTotalElements(creditReferenceList.getTotalElements());
	    creditReferenceLogResponse.setTotalPages(creditReferenceList.getTotalPages());
	    creditReferenceLogResponse.setLastPage(creditReferenceList.isLast());	
	    String encryptUrl = "http://encryptdecrypt-ms/api/encryptPayload";
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    EncodedPayload encodedPayload = new EncodedPayload();
	    Gson gson = new Gson();
	    String data = gson.toJson(creditReferenceLogResponse);
	    JsonObject jsonObject = new JsonParser().parse(data).getAsJsonObject();
	    JsonObject jObj = new JsonObject();
	    jsonObject.add("data", jObj);
	    encodedPayload.setPayload(jsonObject.toString());
	    HttpEntity<EncodedPayload> request = new HttpEntity<>(encodedPayload, headers);
	    String encryptData = restTemplate.postForObject(encryptUrl, request, String.class);
		
		ResponseBean reponsebean=ResponseBean.builder().data(encryptData).status("success").message("All Credit Reference Log fetch Successfull!!").build();
		return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.OK);
	
	}
	
	

	@PostMapping("/depositWithdraw")
	public ResponseEntity<ResponseBean> depositWithdraw(@RequestBody EncodedPayload payload) {
	    EXUser parent = (EXUser) httpSession.getAttribute("EXUser");
	    
	    String decryptUrl = "http://ENCRYPTDECRYPT-MS/api/decryptDepositWithdraw";
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    HttpEntity<EncodedPayload> requestEntity = new HttpEntity<>(payload, headers);
	    DepositWithdraw request = restTemplate.postForObject(decryptUrl, requestEntity, DepositWithdraw.class);
	    String encryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/encode?encode="+request.getPassword(),String.class);
	    
	    List<EXUser> transactions = request.getTransactions();
	    
	    boolean insufficientParentBalance = false;
	    boolean insufficientChildBalance = false;
	    
	    TransactionHistory history = new TransactionHistory();
	    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	    Date date = new Date();
	    
	    if (!parent.getPassword().equals(encryptPassword)) {
	        ResponseBean responseBean = ResponseBean.builder().data("Deposit/Withdraw").status("error").message("wrong password").build();
	        return new ResponseEntity<>(responseBean, HttpStatus.OK);
	    }

	    List<EXUser> updatedUsers = new ArrayList<>();
	    
	    for (EXUser transaction : transactions) {
	        EXUser user = userRepo.findByUserid(transaction.getUserid().toLowerCase());
	        
	         if (transaction.getType().equalsIgnoreCase("deposit")) {
	            if (parent.getMyBalance() >= transaction.getMyBalance()) {
	                user.setMyBalance(user.getMyBalance() + transaction.getMyBalance());
	                parent.setMyBalance(parent.getMyBalance() - transaction.getMyBalance());
	                history.setDepositFromUpline(transaction.getMyBalance());
	                history.setWithdrawByUpline(0.0);	               
	                history.setBalance(user.getMyBalance());
	                history.setFrom(parent.getUserid());
	                history.setTo(user.getUserid());
	                history.setDate_time(sdf.format(date));
	                transactionHistoryRepo.save(history);
	            } else {
	            	insufficientParentBalance = true;
	            }
	        } else if (transaction.getType().equalsIgnoreCase("withdraw")) {
	            if (transaction.getMyBalance() <= user.getMyBalance()) {
	                user.setMyBalance(user.getMyBalance() - transaction.getMyBalance());
	                parent.setMyBalance(parent.getMyBalance() + transaction.getMyBalance());
	                history.setDepositFromUpline(0.0);
	                history.setWithdrawByUpline(transaction.getMyBalance());
	                history.setBalance(user.getMyBalance());
	                history.setFrom(parent.getUserid());
	                history.setTo(user.getUserid());
	                history.setDate_time(sdf.format(date));
	                transactionHistoryRepo.save(history);
	            } else {
	            	insufficientChildBalance = true; 
	            }
	        }
	         updatedUsers.add(user);
	    }
	    userRepo.saveAll(updatedUsers);
	    userRepo.save(parent);
	    
	    if (insufficientParentBalance) {
	        ResponseBean responseBean = ResponseBean.builder().data("Deposit/Withdraw").status("error").message("Admin don't have sufficient balance").build();
	        return new ResponseEntity<>(responseBean, HttpStatus.OK);
	    }
	    
	    if (insufficientChildBalance) {
	        ResponseBean responseBean = ResponseBean.builder().data("Deposit/Withdraw").status("error").message("Child don't have sufficient balance").build();
	        return new ResponseEntity<>(responseBean, HttpStatus.OK);
	    }

	    ResponseBean responseBean = ResponseBean.builder().data("Deposit/Withdraw").status("success").message("Balance updated successfully").build();
	    return new ResponseEntity<>(responseBean, HttpStatus.OK);
	}
	
	@PostMapping("/transactionHistory")
	public ResponseEntity<ResponseBean> transactionHistoryList(@RequestBody EncodedPayload payload, @RequestParam("pageNumber") int pageNumber, @RequestParam("pageSize") int pageSize ){
		
		String decryptData = "http://ENCRYPTDECRYPT-MS/api/decryptPayload";
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    HttpEntity<EncodedPayload> requestEntity = new HttpEntity<>(payload, headers);
	    EXUser user = restTemplate.postForObject(decryptData, requestEntity, EXUser.class);
		
		String userid = user.getUserid();
		Pageable pageable = PageRequest.of(pageNumber, pageSize);
	    Page<TransactionHistory> transactionHistoryList = transactionHistoryRepo.findByfrom(userid, pageable);
	    TransactionHistoryResponse transactionhistoryresponse = new TransactionHistoryResponse();
	    List<TransactionHistory> contents = transactionHistoryList.getContent();
	    transactionhistoryresponse.setContent(contents);
	    transactionhistoryresponse.setPageNumber(transactionHistoryList.getNumber());
	    transactionhistoryresponse.setPageSize(transactionHistoryList.getSize());
	    transactionhistoryresponse.setTotalElements(transactionHistoryList.getTotalElements());
	    transactionhistoryresponse.setTotalPages(transactionHistoryList.getTotalPages());
	    transactionhistoryresponse.setLastPage(transactionHistoryList.isLast());
		String decryptUrl = "http://encryptdecrypt-ms/api/encryptPayload";
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    EncodedPayload encodedPayload=new EncodedPayload();
	    Gson gson = new Gson();
		String data = gson.toJson(transactionhistoryresponse);
	    JsonObject jsonObjects = new JsonParser().parse(data).getAsJsonObject();
	    JsonObject jObjs = new JsonObject();
	    jsonObjects.add("data", jObjs);
	    encodedPayload.setPayload(jsonObjects.toString());
	    HttpEntity<EncodedPayload> request = new HttpEntity<>(encodedPayload, headers);
	    String encryptData = restTemplate.postForObject(decryptUrl, request, String.class);
	    ResponseBean reponsebean=ResponseBean.builder().data(encryptData).status("success").message("All TransactionHistory Details fetch Successfull!!").build();
		return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.OK);
	}
	
	@PostMapping("/activityLog")
	public ResponseEntity<ResponseBean> activityLogList(@RequestBody EncodedPayload payload, @RequestParam("pageNumber") int pageNumber, @RequestParam("pageSize") int pageSize) {
		String decryptData = "http://ENCRYPTDECRYPT-MS/api/decryptPayload";
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    HttpEntity<EncodedPayload> requestEntity = new HttpEntity<>(payload, headers);
	    EXUser user = restTemplate.postForObject(decryptData, requestEntity, EXUser.class);
		
		String userid = user.getUserid();
		Pageable pageable = PageRequest.of(pageNumber, pageSize);
	    Page<ActivityLog> activityList = activityLogRepo.findByuserid(userid, pageable);
	    ActivityLogResponse activitylogresponse = new ActivityLogResponse();
	    List<ActivityLog> contents = activityList.getContent();
	    activitylogresponse.setContent(contents);
	    activitylogresponse.setPageNumber(activityList.getNumber());
	    activitylogresponse.setPageSize(activityList.getSize());
	    activitylogresponse.setTotalElements(activityList.getTotalElements());
	    activitylogresponse.setTotalPages(activityList.getTotalPages());
	    activitylogresponse.setLastPage(activityList.isLast());
	    String encryptUrl = "http://encryptdecrypt-ms/api/encryptPayload";
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    EncodedPayload encodedPayload = new EncodedPayload();
	    Gson gson = new Gson();
	    String data = gson.toJson(activitylogresponse);
	    JsonObject jsonObject = new JsonParser().parse(data).getAsJsonObject();
	    JsonObject jObj = new JsonObject();
	    jsonObject.add("data", jObj);
	    encodedPayload.setPayload(jsonObject.toString());
	    HttpEntity<EncodedPayload> request = new HttpEntity<>(encodedPayload, headers);
	    String encryptData = restTemplate.postForObject(encryptUrl, request, String.class);
	    ResponseBean responseBean = ResponseBean.builder().data(encryptData).status("success").message("All Activitylog Details fetch Successful!!").build();
	    return new ResponseEntity<ResponseBean>(responseBean, HttpStatus.OK);
	}
	
	@PostMapping("/changeCurrentPassword")
	public ResponseEntity<ResponseBean> changeCurrentPassword(@RequestBody EncodedPayload payload ){
		EXUser parent = (EXUser) httpSession.getAttribute("EXUser");
		String decryptUrl = "http://ENCRYPTDECRYPT-MS/api/decryptPayload";
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    HttpEntity<EncodedPayload> requestEntity = new HttpEntity<>(payload, headers);
	    EXUser decryptData = restTemplate.postForObject(decryptUrl, requestEntity, EXUser.class);
	    String newPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/encode?encode="+decryptData.getPassword(),String.class);
	   
	    if(!parent.getPassword().equals(newPassword)) {
	    	ResponseBean reponsebean=ResponseBean.builder().data("Password Updation").status("Error").message("Invalid old Password!!").build();
	    	return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.OK);
	    }
	    
	    if(p.matcher(decryptData.getNewPassword()).matches()== false) {
	    	ResponseBean reponsebean=ResponseBean.builder().data("Password Updation").status("Error").message("Password Must contains 1 Upper Case, 1 Lower Case & 1 Numeric Value & in Between 8-15 Charachter").build();
			return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.OK);
	    }else {
	    	parent.setPassword(decryptData.getNewPassword());
	    	String encryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/encode?encode="+parent.getPassword(),String.class);
	    	parent.setPassword(encryptPassword);
	    	userRepo.save(parent);
	    	ResponseBean reponsebean=ResponseBean.builder().data("Password Updation").status("success").message("Password updated Successfull!!").build();
	    	return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.OK);
	    }
	    
	}
	
	
	@GetMapping("/loginUser")
	public ResponseEntity<ResponseBean> loginUser() {
		EXUser parent = (EXUser) httpSession.getAttribute("EXUser");
		EXUser user = authenticaterepo.findByUserid(parent.getUserid());
		
		String  encryptUrl = "http://ENCRYPTDECRYPT-MS/api/encryptPayload";
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<EXUser> userRequestEntity = new HttpEntity<>(user, headers);
		String encryptUserData = restTemplate.postForObject(encryptUrl, userRequestEntity, String.class);

		ResponseBean reponsebean=ResponseBean.builder().data(encryptUserData).status("success").message("User login Successfull!!").build();
		return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.OK);
		
	}
	
	
	@PostMapping("/searchUser")
	public ResponseEntity<ResponseBean> searchUser(@RequestBody EncodedPayload payload) {
		
		String decryptUrl = "http://ENCRYPTDECRYPT-MS/api/decryptPayload";
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    HttpEntity<EncodedPayload> requestEntity = new HttpEntity<>(payload, headers);
	    EXUser decryptData = restTemplate.postForObject(decryptUrl, requestEntity, EXUser.class);
		
		EXUser user = authenticaterepo.findByUserid(decryptData.getUserid());
		
		String  encryptUrl = "http://ENCRYPTDECRYPT-MS/api/encryptPayload";
	    headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<EXUser> userRequestEntity = new HttpEntity<>(user, headers);
		String encryptUserData = restTemplate.postForObject(encryptUrl, userRequestEntity, String.class);
		
		ResponseBean reponsebean=ResponseBean.builder().data(encryptUserData).status("success").message("User login Successfull!!").build();
		return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.OK);
		
	}
	
	@PostMapping("/depositChips")
	public ResponseEntity<ResponseBean> depositChips(@RequestBody EncodedPayload payload) {
		EXUser parent = (EXUser) httpSession.getAttribute("EXUser");
		EXUser user = userRepo.findByUserid(parent.getUserid());
		
		String decryptUrl = "http://ENCRYPTDECRYPT-MS/api/decryptPayload";
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    HttpEntity<EncodedPayload> requestEntity = new HttpEntity<>(payload, headers);
	    EXUser decryptData = restTemplate.postForObject(decryptUrl, requestEntity, EXUser.class);
	    if(decryptData.getMyBalance()==null || decryptData.getMyBalance()<=0) {
	    	ResponseBean reponsebean=ResponseBean.builder().data("DepositChips").status("Error").message("Enter a valid Chips!!").build();
			return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.OK);
	    }else {
	    	Double value = user.getMyBalance()+decryptData.getMyBalance();
	    	user.setMyBalance(value);
	    	userRepo.save(user);
	    	ResponseBean reponsebean=ResponseBean.builder().data("DepositChips").status("success").message("Chips updated Successfull!!").build();
		    return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.OK);
	    }
	}
	
	@PostMapping("/importantMessage")
	public ResponseEntity<ResponseBean> setImportantMessage(@RequestBody EncodedPayload payload){
		
		String decryptUrl = "http://ENCRYPTDECRYPT-MS/api/decryptPayload";
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    HttpEntity<EncodedPayload> requestEntity = new HttpEntity<>(payload, headers);
	    EXUser decryptData = restTemplate.postForObject(decryptUrl, requestEntity, EXUser.class);
		
		if(decryptData.getPayload()==null || decryptData.getPayload().equalsIgnoreCase("")){
			ResponseBean reponsebean=ResponseBean.builder().data("Important Message").status("Error").message("Enter a valid Message").build();
		    return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.OK);
		}else {
		ImportantMessage message = new ImportantMessage();
		message.setMessage(decryptData.getPayload());
		messageRepo.save(message);
		
		ResponseBean reponsebean=ResponseBean.builder().data("Important message").status("success").message("Message Added Successfull!!").build();
	    return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.OK);
		}
	}
	
	@GetMapping("/currentImportantMessage")
	public ResponseEntity<ResponseBean> listOfMessages() {
		List<ImportantMessage> findAll = messageRepo.findAll();
		ImportantMessage importantMessage = findAll.get(findAll.size()-1);
		String decryptUrl = "http://ENCRYPTDECRYPT-MS/api/encryptPayload";
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    EncodedPayload encodedPayload=new EncodedPayload();
	    Gson gson = new Gson();
		String data = gson.toJson(importantMessage);
		JsonObject jsonArray = new JsonParser().parse(data).getAsJsonObject();
		JSONObject jObj = new JSONObject();
		jObj.put("data", jsonArray);
	    encodedPayload.setPayload(jObj.toString());
	    HttpEntity<EncodedPayload> requestEntity = new HttpEntity<>(encodedPayload, headers);
	    String encryptMessageData = restTemplate.postForObject(decryptUrl, requestEntity, String.class);
	    ResponseBean reponsebean=ResponseBean.builder().data(encryptMessageData).status("success").message("All Messages fetch Successfull!!").build();
		return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.OK);
	}
	
	@PostMapping("/saveHyperMessage") 
	public ResponseEntity<ResponseBean> saveHyperMessage(@RequestBody EncodedPayload payload) {
	  String decryptUrl = "http://ENCRYPTDECRYPT-MS/api/decryptHyperMessage";
	  HttpHeaders headers = new HttpHeaders();
	  headers.setContentType(MediaType.APPLICATION_JSON);
	  HttpEntity<EncodedPayload> requestEntity = new HttpEntity<>(payload, headers);
	  HyperMessage decryptedMessage = restTemplate.postForObject(decryptUrl, requestEntity, HyperMessage.class);
	  hyperMessageRepo.save(decryptedMessage);
	  return ResponseEntity.ok(new ResponseBean("Success", "HyperMessage Created Successfully!!", "HyperMessage")); 
	  }
	
	@GetMapping("/allHyperMessage")
	public ResponseEntity<ResponseBean> listOfHyperMessage() {
		List<HyperMessage> findAll = hyperMessageRepo.findAll();
		String decryptUrl = "http://ENCRYPTDECRYPT-MS/api/encryptPayload";
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    EncodedPayload encodedPayload=new EncodedPayload();
	    Gson gson = new Gson();
		String data = gson.toJson(findAll);
		JsonArray jsonArray = new JsonParser().parse(data).getAsJsonArray();
		JSONObject jObj = new JSONObject();
		jObj.put("data", jsonArray);
	    encodedPayload.setPayload(jObj.toString());
	    HttpEntity<EncodedPayload> requestEntity = new HttpEntity<>(encodedPayload, headers);
	    String encryptwebsiteData = restTemplate.postForObject(decryptUrl, requestEntity, String.class);
	    ResponseBean reponsebean=ResponseBean.builder().data(encryptwebsiteData).status("success").message("All HyperMessage details fetch Successfull!!").build();
		return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.OK);
	}
	
	@PutMapping("/updateHyperMessage")
	public ResponseEntity<ResponseBean> updateHyperMessage(@RequestBody EncodedPayload payload) {		
	    String decryptUrl = "http://ENCRYPTDECRYPT-MS/api/decryptHyperMessage";
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    HttpEntity<EncodedPayload> requestEntity = new HttpEntity<>(payload, headers);
	    HyperMessage decryptedMessage = restTemplate.postForObject(decryptUrl, requestEntity, HyperMessage.class);
	    String id = decryptedMessage.getId();
	    HyperMessage hyperMessage = hyperMessageRepo.findById(id).get();
	    hyperMessage.setMessage(decryptedMessage.getMessage());
	    hyperMessage.setDate(decryptedMessage.getDate());
	    hyperMessage.setTitle(decryptedMessage.getTitle());
	    hyperMessage.setIsLock(decryptedMessage.getIsLock());
	    hyperMessageRepo.save(hyperMessage);
	    return ResponseEntity.ok(new ResponseBean("Success", "HyperMessage Updated Successfully!!", "HyperMessage"));
	}
	
	@DeleteMapping("/deleteHyperMessage")
	public ResponseEntity<ResponseBean> deleteHyperMessage(@RequestBody EncodedPayload payload) {		
	    String decryptUrl = "http://ENCRYPTDECRYPT-MS/api/decryptHyperMessage";
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    HttpEntity<EncodedPayload> requestEntity = new HttpEntity<>(payload, headers);
	    HyperMessage decryptedMessage = restTemplate.postForObject(decryptUrl, requestEntity, HyperMessage.class);
	    String id = decryptedMessage.getId();
	    HyperMessage hyperMessage = hyperMessageRepo.findById(id).get();
	    hyperMessageRepo.delete(hyperMessage);
	    return ResponseEntity.ok(new ResponseBean("Success", "HyperMessage Deleted Successfully!!", "HyperMessage"));
	}
}