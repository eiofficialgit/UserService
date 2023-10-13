
package com.example.controller;


import java.io.IOException; 
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.data.domain.Sort;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import com.example.entity.ActivityLog;
import com.example.entity.ActivityLogResponse;
import com.example.entity.CreditReferenceLog;
import com.example.entity.CreditReferenceLogResponse;
import com.example.entity.DecryptResponse;
import com.example.entity.DepositWithdraw;
import com.example.entity.EXUser;
import com.example.entity.EXUserData;
import com.example.entity.EXUserResponse;
import com.example.entity.EncodedPayload;
import com.example.entity.HyperMessage;
import com.example.entity.ImageData;
import com.example.entity.ImportantMessage;
import com.example.entity.Match;
import com.example.entity.MatchClose;
import com.example.entity.Odds;
import com.example.entity.Partnership;
import com.example.entity.ResponseBean;
import com.example.entity.Runners;
import com.example.entity.TransactionHistory;
import com.example.entity.TransactionHistoryResponse;
import com.example.entity.UserStake;
import com.example.entity.WebsiteBean;
import com.example.repository.ActivityLogRepo;
import com.example.repository.Authenticaterepo;
import com.example.repository.CreditReferenceLogRepo;
import com.example.repository.EXUserDataRepo;
import com.example.repository.EXUserRepository;
import com.example.repository.HyperMessageRepo;
import com.example.repository.ImportantMessageRepo;
import com.example.repository.MatchCloseRepo;
import com.example.repository.MatchRepo;
import com.example.repository.StorageRepo;
import com.example.repository.TransactionHistoryRepo;
import com.example.repository.WebsiteBeanRepository;
import com.example.service.ImageUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@CrossOrigin("*")
@RequestMapping("/exuser")
@EnableScheduling
@EnableCaching
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
	private MatchRepo matchRepo;
	
	@Autowired
	private CreditReferenceLogRepo creditReferenceLogRepo;
	
	@Autowired
	private MatchCloseRepo matchCloseRepo;
	
	@Autowired
	private EXUserDataRepo exUserDataRepo;
	
	@Autowired
	private StorageRepo storageRepo;
	
	

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
		ResponseBean responseBean = new ResponseBean();
		String decryptUrl = "http://ENCRYPTDECRYPT-MS/api/decryptPayload";
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    HttpEntity<EncodedPayload> requestEntity = new HttpEntity<>(payload, headers);
	    EXUser childData = restTemplate.postForObject(decryptUrl, requestEntity, EXUser.class);
		
		
		try {
			EXUser checkUser = userRepo.findByUserid(((EXUser) childData).getUserid().toLowerCase());
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
			if (((EXUser) childData).getUsertype() == 0) {
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
			} else if (((EXUser) childData).getUsertype() == 1) {
				EXUser saveMiniAdmin = saveMiniAdmin(childData);
				if (childData.getUserid() != null) {
					userRepo.save(saveMiniAdmin);
					responseBean.setData("success");
					responseBean.setMessage("Success");
					responseBean.setStatus("Success");
					return new ResponseEntity<Object>(responseBean, HttpStatus.OK);
				}
			}else if(((EXUser) childData).getUsertype() == 2){
				EXUser saveSuperSuper = saveSuperSuper(childData);
				if(childData.getUserid()!=null){
					userRepo.save(saveSuperSuper);
					responseBean.setData("success");
					responseBean.setMessage("Success");
					responseBean.setStatus("Success");
					return new ResponseEntity<Object>(responseBean,HttpStatus.OK);
				}
			}else if(((EXUser) childData).getUsertype() == 3){
				EXUser saveSuperMaster = saveSuperMaster(childData);
				if(childData.getUserid()!=null){
					userRepo.save(saveSuperMaster);
					responseBean.setData("success");
					responseBean.setMessage("Success");
					responseBean.setStatus("Success");
					return new ResponseEntity<Object>(responseBean,HttpStatus.OK);
				}
			}else if(((EXUser) childData).getUsertype() == 4){
				EXUser saveMaster = saveMaster(childData);
				if(childData.getUserid()!=null){
					userRepo.save(saveMaster);
					responseBean.setData("success");
					responseBean.setMessage("Success");
					responseBean.setStatus("Success");
					return new ResponseEntity<Object>(responseBean,HttpStatus.OK);
				}
			}else if(((EXUser) childData).getUsertype() == 5){
				EXUser saveUser = saveUser(childData);
				if(childData.getUserid()!=null){
					userRepo.save(saveUser);
					responseBean.setData("success");
					responseBean.setMessage("Success");
					responseBean.setStatus("Success");
					return new ResponseEntity<Object>(responseBean,HttpStatus.OK);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		responseBean.setData("error");
		responseBean.setMessage("Not Authorized to Create this type of User");
		responseBean.setStatus("Error");
		return new ResponseEntity<Object>(responseBean, HttpStatus.ACCEPTED);

	}

	public EXUser saveSubAdmin(EXUser user) throws Exception {
		EXUser parent = userRepo.findById(user.getId()).get();
		
		EXUser child = new EXUser();
		String encryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/encode?encode="+user.getPassword(),String.class);
		child.setPassword(encryptPassword);
		try {

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
			child.setMyBalance(0.0);
			child.setFixLimit(0.0);
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
			e.printStackTrace();
		}
		

		return child;
	}

	public EXUser saveMiniAdmin(EXUser user) {
		
		EXUser parent = userRepo.findById(user.getId()).get();
		EXUser child = new EXUser();
		String encryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/encode?encode="+user.getPassword(),String.class);
		child.setPassword(encryptPassword);
		try {
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
			e.printStackTrace();
		}

		return child;
	}
	
	public EXUser saveSuperSuper(EXUser user){
		EXUser parent = userRepo.findById(user.getId()).get();
		EXUser child = new EXUser();
		String encryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/encode?encode="+user.getPassword(),String.class);
		child.setPassword(encryptPassword);
		try {

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
			e.printStackTrace();
		}
		
		
		return child;
	}
	
	
	public EXUser saveSuperMaster(EXUser user){
		EXUser parent = userRepo.findById(user.getId()).get();
		EXUser child = new EXUser();
		String encryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/encode?encode="+user.getPassword(),String.class);
		child.setPassword(encryptPassword);
		try{

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
			e.printStackTrace();
		}
		
		
		return child;
	}
	
	
	public EXUser saveMaster(EXUser user){
		EXUser parent = userRepo.findById(user.getId()).get();
		EXUser child = new EXUser();
		String encryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/encode?encode="+user.getPassword(),String.class);
		child.setPassword(encryptPassword);
		try{
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
			e.printStackTrace();
		}
		
		
		return child;
	}
	
	
	public EXUser saveUser(EXUser user){
		EXUser parent = userRepo.findById(user.getId()).get();
		EXUser child = new EXUser();
		String encryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/encode?encode="+user.getPassword(),String.class);
		child.setPassword(encryptPassword);
		try{

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
			e.printStackTrace();
		}
		
		
		return child;
	}


	@Resource(name ="redisTemplate")
	private HashOperations<String, String, EXUserData> hashOperations;

	@PostMapping("/managementHome")
	public ResponseEntity<ResponseBean> managementHome(@RequestBody EncodedPayload payload) {
		
		String decryptUrl = "http://ENCRYPTDECRYPT-MS/api/decryptPayload";
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    HttpEntity<EncodedPayload> requestEntity = new HttpEntity<>(payload, headers);
	    DecryptResponse decryptData = restTemplate.postForObject(decryptUrl, requestEntity, DecryptResponse.class);
	    String encryptPassword = restTemplate.getForObject("http://ENCRYPTDECRYPT-MS/api/encode?encode="+decryptData.getPassword(),String.class);
		EXUser user = authenticaterepo.findByUserid(decryptData.getUserid());
		httpSession.invalidate();
		user.setSessionId(httpSession.getId());
		authenticaterepo.save(user);
		
		EXUserData findByUserid = exUserDataRepo.findByUserid(decryptData.getUserid());
		EXUserData data = new EXUserData();
		if(findByUserid==null) {
			data.setSessionId(RequestContextHolder.currentRequestAttributes().getSessionId());
			data.setUserid(decryptData.getUserid());
			exUserDataRepo.save(data);
			}else {
				EXUserData exUserData = exUserDataRepo.findById(findByUserid.getId()).get();
				exUserData.setSessionId(RequestContextHolder.currentRequestAttributes().getSessionId());
				exUserDataRepo.save(exUserData);
				hashOperations.put("EXUserData", exUserData.getId(), exUserData);
			}
		
		if(user==null) {
			ResponseBean reponsebean=ResponseBean.builder().data("ManagementHome").status("Error").message("Wrong UserId!!!").build();
		return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.UNAUTHORIZED);
		}
		
		
		if(!user.getPassword().equals(encryptPassword)) {
			ResponseBean reponsebean=ResponseBean.builder().data("ManagementHome").status("Error").message("Wrong password!!!").build();
		return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.UNAUTHORIZED);
		}
		
		if (!user.getIsActive()) {
            ResponseBean responseBean = ResponseBean.builder().data("ManagementHome").status("Error").message("Account Locked Please contact the Admin").build();
            return new ResponseEntity<>(responseBean, HttpStatus.UNAUTHORIZED);
        }
		
		EXUser users = authenticaterepo.findByUserid(decryptData.getUserid());
		
		httpSession.setAttribute("EXUser", users);
		
		ActivityLog log = new ActivityLog();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	    Date date = new Date();
	    log.setUserid(users.getUserid());
	    log.setDate_time(sdf.format(date));
	    log.setIpAddress(httpServletRequest.getRemoteAddr());
	    log.setLoginStatus("Login--");
	    activityLogRepo.save(log);
	    System.out.println(httpServletRequest.getRequestedSessionId());
		
		String  encryptUrl = "http://ENCRYPTDECRYPT-MS/api/encryptPayload";
		HttpEntity<EXUser> userRequestEntity = new HttpEntity<>(users, headers);
		String encryptUserData = restTemplate.postForObject(encryptUrl, userRequestEntity, String.class);

		ResponseBean reponsebean=ResponseBean.builder().data(encryptUserData).status("success").message("User login Successfull!!").build();
		return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.OK);
	}
	
	
	@GetMapping(value = "/{key}", consumes = "application/json" )
	public EXUserData getData(@PathVariable String key) {
		return hashOperations.get("EXUserData", key);	
	}
	
	
	@GetMapping("/logout")
	public ResponseEntity<ResponseBean> logout(HttpServletRequest request){
		EXUser exUser = (EXUser) httpSession.getAttribute("EXUser");
		EXUserData user = exUserDataRepo.findByUserid(exUser.getUserid());
		EXUserData exUserData = hashOperations.get("EXUserData", user.getId());
		System.out.println(exUserData.getSessionId());
		if(exUserData.getSessionId().equals(RequestContextHolder.currentRequestAttributes().getSessionId())) {
			return ResponseEntity.ok(new ResponseBean("success", " user still loggedIn!!", "ManagementHome"));
		   }else {
			HttpSession session = request.getSession(false);
			session.invalidate();
			return ResponseEntity.ok(new ResponseBean("success", "Logout Successfully!!", "ManagementHome"));
		  }

	}
	
	
	
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
		
		EXUser exUser = userRepo.findById(parentId).get();
		
		if(exUser.getBetLock()==false) {
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
		}else {
			ResponseBean responseBean = ResponseBean.builder().data("Downline List").status("Error").message("Account Suspended Please contact the Admin").build();
		    return new ResponseEntity<>(responseBean, HttpStatus.OK);
		}
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
	
	
	@Scheduled(fixedRate = 900000)
	@GetMapping("/allMatches")
    public JsonNode allMatches() {
        Request request = new Request.Builder()
                .url("https://data.mainredis.in/api/match/getMatches?isActive=true&isResult=false&type=own&sportId=124")
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        OkHttpClient httpClient = new OkHttpClient();
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                JsonNode responseBody = objectMapper.readTree(response.body().string());
                
                JsonNode results = responseBody.get("result");
                
                List<Match> matchesToSave = new ArrayList<>();
                for (JsonNode matchNode : results) {
                	Match match = objectMapper.treeToValue(matchNode, Match.class);
                	Match findByeventId = matchRepo.findByMarketId(match.getMarketId());
            		if(findByeventId == null) {
            		match.setisActive(true);
                    matchesToSave.add(match);
                }  
                }
                matchRepo.saveAll(matchesToSave);
                              
                return responseBody;
            } else {
                return objectMapper.createObjectNode().put("error", "HTTP error: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return objectMapper.createObjectNode().put("error", "Exception: " + e.getMessage());
        }
    }
	
	
	@Scheduled(fixedRate = 900000)
	@GetMapping("/statusCheck")
	public ResponseEntity<Object> statusCheck() {
	    try {
	        List<Match> matchList = matchRepo.findByisActive(true);

	        ObjectMapper objectMapper = new ObjectMapper();

	        List<Match> matchesToSave = new ArrayList<>();
	        List<MatchClose> matchCloseToSave = new ArrayList<>();

	        for (Match match : matchList) {
	            String apiUrl = "https://scoreapi.365cric.com/api/match/getResult?mktid=" + match.getMarketId();
	            HttpHeaders headers = new HttpHeaders();
	            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
	            HttpEntity<String> entity = new HttpEntity<>(headers);
	            RestTemplate restTemplate = new RestTemplate();
	            ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);
	            String response = responseEntity.getBody();
	            if (response != null) {
	                JsonNode jsonResponse = objectMapper.readTree(response);

	                if (jsonResponse.isArray()) {
	                    for (JsonNode matchNode : jsonResponse) {
	                        if (matchNode.has("status") && matchNode.get("status").asText().equals("CLOSED")) {
	                        	match.setisActive(false);
	                             match.setisResult(true);
	                            matchesToSave.add(match);
	                            MatchClose matchClose = new MatchClose();
	                            matchClose.setMarketid(match.getEventId());
	                            matchClose.setMarketname(match.getMarketName());
	                            matchClose.setSportid(match.getSportId());
	                            matchClose.setSportname(match.getSportName());
	                            matchClose.setMatchid(match.getEventId());
	                            matchClose.setMatchname(match.getEventName());
	                            matchClose.setEventDateTime(match.getOpenDate());
	                            matchClose.setResultIP("AutoMatic");
	                            matchClose.setStatus(true);
	                            if (matchNode.has("runners") && matchNode.get("runners").isArray()) {
	                                for (JsonNode runnerNode : matchNode.get("runners")) {
	                                	if (runnerNode.has("status") && runnerNode.get("status").asText().equals("WINNER")) {
	                                		int winnerSelectionId = runnerNode.get("selectionId").asInt();
	                                		for (Runners runner : match.getMatchRunners()) {
	                                            if (runner.getSelectionId() == winnerSelectionId) {
	                                                matchClose.setSelectionid(runner.getSelectionId());	 
	                                                matchClose.setSelectionname(runner.getName());
	                                                break; 
	                                            }
	                                        }
	                                    }
	                                }
	                            }
	                            matchCloseToSave.add(matchClose);
	                        }
	                    }
	                }
	            }
	        }
	        matchCloseRepo.saveAll(matchCloseToSave);
	        matchRepo.saveAll(matchesToSave);

	        return new ResponseEntity<>(HttpStatus.OK);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred.");
	    }
	}

	
	
	@GetMapping("/allSaveMatches")
	public List<Match> allSaveMatches(@PathVariable boolean isActive) {
		 List<Match> findAll = matchRepo.findByisActive(true);
		 return findAll;
	}
	
	
	@PostMapping("/activeInactiveMatches")
	public ResponseEntity<ResponseBean> allSaveMatches(@RequestBody Match match) {
		Match findByeventId = matchRepo.findByMarketId(match.getMarketId());
		if(match.isActive()) {
		findByeventId.setisActive(true);
		matchRepo.save(findByeventId);
		ResponseBean reponsebean=ResponseBean.builder().data("MatchEntity").status("success").message(findByeventId.getEventName() +" Active Successfully!!").build();
	    return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.OK);
		}else {
			findByeventId.setisActive(false);
			matchRepo.save(findByeventId);
			ResponseBean reponsebean=ResponseBean.builder().data("MatchEntity").status("success").message(findByeventId.getEventName() +" Inactive Successfully!!").build();
		    return new ResponseEntity<ResponseBean>(reponsebean, HttpStatus.OK);
		}
	}
	    
	
	
	@PostMapping("/match")
	public List<Match> getMatch(@RequestBody Match match){
		
		String competitionId = match.getCompetitionId();
		String eventId = match.getEventId();
		if(competitionId!=null && eventId==null) {
			List<Match> competitionList = matchRepo.findBycompetitionId(competitionId);
			return competitionList;
		}else if(competitionId==null && eventId!=null){
			List<Match> eventList = matchRepo.findByeventId(eventId);
			return eventList;
		}
		return null;
	}
	
	
	@GetMapping("/getMatchList")
	public ResponseEntity<Object> getMatchList(
	        @RequestParam(required = false) Integer sportId,
	        @RequestParam(required = false) Boolean isActive) {
	    try {
	        HttpHeaders headers = new HttpHeaders();
	        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	        HttpEntity<String> entity = new HttpEntity<>(headers);
	        RestTemplate restTemplate = new RestTemplate();  
	        String apiUrl = "https://data.mainredis.in/api/match/getMatches?isActive=true&isResult=false&type=own";

	        if (sportId != null && sportId > 0) {
	            apiUrl += "&sportId=" + sportId;
	        }
	        ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl,HttpMethod.GET,entity,String.class);
	        String response = responseEntity.getBody();
	        ObjectMapper objectMapper = new ObjectMapper();
	        JsonNode root = objectMapper.readTree(response);
	        JsonNode events = root.get("result");

	        JSONArray sportArray = new JSONArray();

	        for (JsonNode eventNode : events) {
	            JSONObject event = new JSONObject(eventNode.toString());
	            String eventId = event.getString("eventId");
	            event.put("isAdded", matchRepo.existsByEventId(eventId));
	            JSONObject fineEvent = new JSONObject();
	            fineEvent.put("results", event);
  
	            int sportId1 = event.getInt("sportId"); 
	            if ((sportId == null && sportId1 > 0) || (sportId != null && sportId1 == sportId)) {
	                sportArray.put(fineEvent);
	            }
	        }

	        return new ResponseEntity<>(sportArray.toString(), HttpStatus.OK);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return new ResponseEntity<>("Error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	    
        
	    @Scheduled(fixedRate = 300000)
        @PostMapping("/updateOdds")
        public ResponseEntity<String> createPost() throws JsonMappingException, JsonProcessingException {
            List<Match> findAll = matchRepo.findByisActive(true);

            String apiUrl = "https://wonderbets.in/api/getOddsData";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ObjectMapper objectMapper = new ObjectMapper();

            for (Match match : findAll) {
                String marketId = match.getMarketId();                String postJson = "{\"marketIds\": \"" + marketId + "\"}";
                HttpEntity<String> requestEntity = new HttpEntity<>(postJson, headers);
                ResponseEntity<String> responseEntity = restTemplate.postForEntity(apiUrl, requestEntity, String.class);
                String response = responseEntity.getBody();

                if (response != null) {
                    JsonNode jsonResponse = objectMapper.readTree(response);
                    Odds oddsData = new Odds();
                    if (jsonResponse.has(marketId)) {
                        JsonNode marketNode = jsonResponse.get(marketId);
                        
                        if (marketNode.has("tiger") && marketNode.get("tiger").isArray()) {
                            JsonNode tigerArray = marketNode.get("tiger");
                            for (JsonNode tigerItem : tigerArray) {
                                if (tigerItem.has("runners") && tigerItem.get("runners").isArray()) {
                                    JsonNode runnersArray = tigerItem.get("runners");
                                       JsonNode firstRunner = runnersArray.get(0);
                                        if(firstRunner.has("selectionId")) {
                                            int selectionId = firstRunner.get("selectionId").asInt();
                               		              for (Runners runner : match.getMatchRunners()) {
    			                                       if (runner.getSelectionId() == selectionId) {
    			                                    	   if(firstRunner.has("ex")) {
    			                                    		   JsonNode exArray = firstRunner.get("ex");
    			                                    		   if (exArray.has("availableToBack") && exArray.has("availableToLay")) {
    			                                       	        JsonNode availableToBack = exArray.get("availableToBack");
    			                                       	        JsonNode availableToLay = exArray.get("availableToLay");
    			                                       	        if (availableToBack.isArray() && availableToLay.isArray() &&
    			                                       	            availableToBack.size() >= 3 && availableToLay.size() >= 3) {
    			                                        	        if (availableToBack.isArray() && availableToLay.isArray() &&
    			                                            	            availableToBack.size() >= 3 && availableToLay.size() >= 3) {
    			                                            	            JsonNode firstBack = availableToBack.get(0);
    			                                            	            JsonNode firstLay = availableToLay.get(0);
    			                                            	            if (firstBack.has("price") && firstBack.has("size") &&
    			                                            	                firstLay.has("price") && firstLay.has("size")) {
//    			                                            	                oddsData.setB1(firstBack.get("price").asDouble()); 
//    			                                            	                oddsData.setL1(firstLay.get("price").asDouble());
    			                                            	                oddsData.b1 = firstBack.get("price").asDouble();
    			                                            	                oddsData.l1 = firstLay.get("price").asDouble();
    			                                            	             }
    			                                       	          }
    			                                       	        }
    			                                       }
                                                    }
                                               }
                                           }
                                        
                                        JsonNode secondRunner = runnersArray.get(1);
                                        if(secondRunner.has("selectionId")) {
                                            int selectionIds = secondRunner.get("selectionId").asInt();
                               		              for (Runners runner : match.getMatchRunners()) {
    			                                       if (runner.getSelectionId() == selectionIds) {
    			                                    	   if(secondRunner.has("ex")) {
    			                                    		   JsonNode exArray = secondRunner.get("ex");
    			                                    		   if (exArray.has("availableToBack") && exArray.has("availableToLay")) {
    			                                       	        JsonNode availableToBack = exArray.get("availableToBack");
    			                                       	        JsonNode availableToLay = exArray.get("availableToLay");
    			                                       	        if (availableToBack.isArray() && availableToLay.isArray() &&
    			                                       	            availableToBack.size() >= 3 && availableToLay.size() >= 3) {
    			                                        	        if (availableToBack.isArray() && availableToLay.isArray() &&
    			                                            	            availableToBack.size() >= 3 && availableToLay.size() >= 3) {
    			                                            	            JsonNode thirdBack = availableToBack.get(2);
    			                                            	            JsonNode thirdLay = availableToLay.get(2);
    			                                            	            if (thirdBack.has("price") && thirdBack.has("size") &&
    			                                            	            		thirdLay.has("price") && thirdLay.has("size")) {
//    			                                            	                oddsData.setB3(thirdBack.get("price").asDouble());
//    			                                            	                oddsData.setL3(thirdLay.get("price").asDouble());
    			                                            	                oddsData.b3 = thirdBack.get("price").asDouble();
    			                                            	                oddsData.l3 = thirdLay.get("price").asDouble();
    			                                            	             }
    			                                        	        }
    			                                       	          }
    			                                       	        }
    			                                       }
                                                    }
                                               }
                                           }
                                        JsonNode thirdRunner = runnersArray.get(2);
                                        if(thirdRunner!=null) {
                                        if(thirdRunner.has("selectionId")) {
                                            int selectionIds = thirdRunner.get("selectionId").asInt();
                               		              for (Runners runner : match.getMatchRunners()) {
    			                                       if (runner.getSelectionId() == selectionIds) {
    			                                    	   if(thirdRunner.has("ex")) {
    			                                    		   JsonNode exArray = thirdRunner.get("ex");
    			                                    		   if (exArray.has("availableToBack") && exArray.has("availableToLay")) {
    			                                       	        JsonNode availableToBack = exArray.get("availableToBack");
    			                                       	        JsonNode availableToLay = exArray.get("availableToLay");
    			                                       	        if (availableToBack.isArray() && availableToLay.isArray() &&
    			                                       	            availableToBack.size() >= 3 && availableToLay.size() >= 3) {
    			                                        	        if (availableToBack.isArray() && availableToLay.isArray() &&
    			                                            	            availableToBack.size() >= 3 && availableToLay.size() >= 3) {
    			                                            	            JsonNode secondBack = availableToBack.get(1);
    			                                            	            JsonNode secondLay = availableToLay.get(1);
    			                                            	            if (secondBack.has("price") && secondBack.has("size") &&
    			                                            	            		secondLay.has("price") && secondLay.has("size")) {
//    			                                            	                oddsData.setB2(secondBack.get("price").asDouble());
//    			                                            	                oddsData.setL2(secondLay.get("price").asDouble()); 
    			                                            	                oddsData.b2 = secondBack.get("price").asDouble();
    			                                            	                oddsData.l2 = secondLay.get("price").asDouble();
    			                                            	              }
    			                                        	        }
    			                                       	          }
    			                                       	        }
    			                                       }
                                                    }
                                               }
                                           }
                                        }
                                        }
                                   }
                            }
                                match.setOdds(oddsData);
                                
                            matchRepo.save(match);
                        }
                        
                    }
                }
            }

            return ResponseEntity.ok("Processed all marketIds.");
        }

        
        
        @PostMapping("/singleOdds")
        public ResponseEntity<String> createPost(@RequestBody String postJson) throws JsonMappingException, JsonProcessingException {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(postJson);

            String marketIds = jsonNode.get("marketIds").asText();
            Match match = matchRepo.findByMarketId(marketIds);

            String apiUrl = "https://wonderbets.in/api/getOddsData";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> requestEntity = new HttpEntity<>(postJson, headers);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(apiUrl, requestEntity, String.class);
            String response = responseEntity.getBody();

            if (response != null) {
                JsonNode jsonResponse = objectMapper.readTree(response);
                Odds oddsData = new Odds();
                if (jsonResponse.has(marketIds)) {
                    JsonNode marketNode = jsonResponse.get(marketIds);
                    if (marketNode.has("tiger") && marketNode.get("tiger").isArray()) {
                        JsonNode tigerArray = marketNode.get("tiger");
                        for (JsonNode tigerItem : tigerArray) {
                            if (tigerItem.has("runners") && tigerItem.get("runners").isArray()) {
                                JsonNode runnersArray = tigerItem.get("runners");
                                   JsonNode firstRunner = runnersArray.get(0);
                                    if(firstRunner.has("selectionId")) {
                                        int selectionId = firstRunner.get("selectionId").asInt();
                           		              for (Runners runner : match.getMatchRunners()) {
			                                       if (runner.getSelectionId() == selectionId) {
			                                    	   if(firstRunner.has("ex")) {
			                                    		   JsonNode exArray = firstRunner.get("ex");
			                                    		   if (exArray.has("availableToBack") && exArray.has("availableToLay")) {
			                                       	        JsonNode availableToBack = exArray.get("availableToBack");
			                                       	        JsonNode availableToLay = exArray.get("availableToLay");
			                                       	        if (availableToBack.isArray() && availableToLay.isArray() &&
			                                       	            availableToBack.size() >= 3 && availableToLay.size() >= 3) {
			                                        	        if (availableToBack.isArray() && availableToLay.isArray() &&
			                                            	            availableToBack.size() >= 3 && availableToLay.size() >= 3) {
			                                            	            JsonNode firstBack = availableToBack.get(0);
			                                            	            JsonNode firstLay = availableToLay.get(0);
			                                            	            if (firstBack.has("price") && firstBack.has("size") &&
			                                            	                firstLay.has("price") && firstLay.has("size")) {
			                                            	               
			                                            	                oddsData.b1 = firstBack.get("price").asDouble();
			                                            	                oddsData.l1 = firstLay.get("price").asDouble();
			                                            	                
			                                        	        }
			                                       	          }
			                                       	        }
			                                       }
                                                }
                                           }
                                       }
                                    
                                    JsonNode secondRunner = runnersArray.get(1);
                                    if(secondRunner.has("selectionId")) {
                                        int selectionIds = secondRunner.get("selectionId").asInt();
                           		              for (Runners runner : match.getMatchRunners()) {
			                                       if (runner.getSelectionId() == selectionIds) {
			                                    	   if(secondRunner.has("ex")) {
			                                    		   JsonNode exArray = secondRunner.get("ex");
			                                    		   if (exArray.has("availableToBack") && exArray.has("availableToLay")) {
			                                       	        JsonNode availableToBack = exArray.get("availableToBack");
			                                       	        JsonNode availableToLay = exArray.get("availableToLay");
			                                       	        if (availableToBack.isArray() && availableToLay.isArray() &&
			                                       	            availableToBack.size() >= 3 && availableToLay.size() >= 3) {
			                                        	        if (availableToBack.isArray() && availableToLay.isArray() &&
			                                            	            availableToBack.size() >= 3 && availableToLay.size() >= 3) {
			                                            	            JsonNode thirdBack = availableToBack.get(0);
			                                            	            JsonNode thirdLay = availableToLay.get(0);
			                                            	            if (thirdBack.has("price") && thirdBack.has("size") &&
			                                            	            		thirdLay.has("price") && thirdLay.has("size")) {
			                                            	                
			                                            	                oddsData.b3 = thirdBack.get("price").asDouble();
			                                            	                oddsData.l3 = thirdLay.get("price").asDouble();
			                                            	            }
			                                        	        }
			                                       	          }
			                                       	        }
			                                       }
                                                }
                                           }
                                       }
                                    JsonNode thirdRunner = runnersArray.get(2);
                                    if(thirdRunner!=null) {
                                    if(thirdRunner.has("selectionId")) {
                                        int selectionIds = thirdRunner.get("selectionId").asInt();
                           		              for (Runners runner : match.getMatchRunners()) {
			                                       if (runner.getSelectionId() == selectionIds) {
			                                    	   if(thirdRunner.has("ex")) {
			                                    		   JsonNode exArray = thirdRunner.get("ex");
			                                    		   if (exArray.has("availableToBack") && exArray.has("availableToLay")) {
			                                       	        JsonNode availableToBack = exArray.get("availableToBack");
			                                       	        JsonNode availableToLay = exArray.get("availableToLay");
			                                       	        if (availableToBack.isArray() && availableToLay.isArray() &&
			                                       	            availableToBack.size() >= 3 && availableToLay.size() >= 3) {
			                                        	        if (availableToBack.isArray() && availableToLay.isArray() &&
			                                            	            availableToBack.size() >= 3 && availableToLay.size() >= 3) {
			                                            	            JsonNode secondBack = availableToBack.get(0);
			                                            	            JsonNode secondLay = availableToLay.get(0);
			                                            	            if (secondBack.has("price") && secondBack.has("size") &&
			                                            	            		secondLay.has("price") && secondLay.has("size")) {
			                                            	                
			                                            	                oddsData.b2 = secondBack.get("price").asDouble();
			                                            	                oddsData.l2 = secondLay.get("price").asDouble();
			                                            	            }
			                                        	        }
			                                       	          }
			                                       	        }
			                                       }
                                                }
                                           }
                                       }
                                    }
                                    }
                               }
                        }
//                            if (match.getOdds() == null) {
//                                match.setOdds(new ArrayList<>());
//                            }

//                            match.getOdds().add(oddsData);
                        match.setOdds(oddsData);
                            
                        matchRepo.save(match);
                    }
                }
            }

            return ResponseEntity.ok(response);
        }

        
        
        @PostMapping("/image")
        public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile file) throws IOException{
        	
        	ImageData imageData = new ImageData();
        	imageData.setName(file.getOriginalFilename());
        	imageData.setType(file.getContentType());
        	imageData.setImageData(ImageUtils.compressImage(file.getBytes()));
        	ImageData save = storageRepo.save(imageData);
        	if(save!=null) {
        	return ResponseEntity.status(HttpStatus.OK).body("file uploaded successfully : "+file.getOriginalFilename())  ;
        	}else {
        		return null;
        	}
        }
        
        @GetMapping("/{fileName}")
        public ResponseEntity<?> downloadImage(@PathVariable String fileName){
        	Optional<ImageData> imageData = storageRepo.findByName(fileName);
        	byte[] image =ImageUtils.decompressImage(imageData.get().getImageData());
        	return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.valueOf("image/png")).body(image);       	
        }


        

	    
	@GetMapping("/getsportid/{sportid}")
	public List<Match> getMatchesBySportId(@PathVariable String sportid) {
        //SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss a");
	    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	    String todayDate = dateFormat.format(new Date());
	    Sort sort = Sort.by(Sort.Direction.ASC, "openDate");
	    List<Match> matches = matchRepo.findByOpenDateAfterOrderByOpenDateAsc(todayDate, sort);

	    if (sportid != null) {
	        List<Match> sportMatches = matchRepo.findBySportIdAndIsActive(sportid, true);

	        if (!matches.isEmpty()) {
	            matches.retainAll(sportMatches);
	        } else {
	            matches = sportMatches;
	        }
	    }

	    return matches;
	}
	
   
        @GetMapping("/getByCompetitionName/{competitionname}")
        public List<Match> getByCompetitionNames(@PathVariable String competitionname) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            String todayDate = dateFormat.format(new Date());
            List<Match> matches = matchRepo.findByOpenDateGreaterThanEqual(todayDate);
            
            if (competitionname != null) {
                List<Match> competitionMatches = matchRepo.findByCompetitionName(competitionname);

                if (!matches.isEmpty()) {
                    matches.retainAll(competitionMatches);
                } else {
                    matches = competitionMatches;
                }
            } 
            
            return matches;
	}
	
        
        
        @GetMapping("/competitionList/{sportid}")
        public List<String> getMatchesList(@PathVariable String sportid) {
        	List<Match> findAll = matchRepo.findBySportId(sportid);
        	 List<String> uniqueCompetitionNames = findAll.stream().map(Match::getCompetitionName).distinct().collect(Collectors.toList());
        	 return uniqueCompetitionNames;
        }

        @GetMapping("/getsportid/{sportid}/{eventid}")
        public List<Match> getMatchBySportAndEventId(@PathVariable String sportid, @PathVariable String eventid) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            String todayDate = dateFormat.format(new Date());
            Sort sort = Sort.by(Sort.Direction.ASC, "openDate");
            
            List<Match> match = matchRepo.findBySportIdAndEventIdAndOpenDateAfter(sportid, eventid, todayDate, sort);

            return match;
        }
        
        
        
        
	

}