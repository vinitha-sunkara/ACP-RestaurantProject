package com.taj.dao.impl;

import com.taj.dao.OrderDao;
import com.taj.entity.Order;
import com.taj.entity.OrderItems;
import com.taj.entity.User;
import com.taj.model.ItemInfo;
import com.taj.model.ShoppingCart;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

public class OrderDaoImpl implements OrderDao {

	private SessionFactory sessionFactory;
	HashMap<String,String> emailPremium = new HashMap<String,String>();

	public void writePremiumUsers(){
		//First User
		JSONObject userDetails = new JSONObject();
		userDetails.put("firstName", "Lakshmi");
		userDetails.put("lastName", "Devi");
		userDetails.put("email", "abc@gmail.com");
		userDetails.put("premiumuser", "true");
		userDetails.put("semipremiumuser", "false");
		userDetails.put("nonpremiumuser", "false");

		JSONObject userObject = new JSONObject();
		userObject.put("user", userDetails);

		//Second User
		JSONObject userDetails2 = new JSONObject();
		userDetails2.put("firstName", "Brian");
		userDetails2.put("lastName", "Schultz");
		userDetails2.put("email", "def@gmail.com");
		userDetails2.put("premiumuser", "false");
		userDetails2.put("semipremiumuser", "true");
		userDetails2.put("nonpremiumuser", "false");

		JSONObject userObject2 = new JSONObject();
		userObject2.put("user", userDetails2);

		//Third User
		JSONObject userDetails3 = new JSONObject();
		userDetails3.put("firstName", "Range");
		userDetails3.put("lastName", "Rover");
		userDetails3.put("email", "stk@gmail.com");
		userDetails3.put("premiumuser", "false");
		userDetails3.put("semipremiumuser", "false");
		userDetails3.put("nonpremiumuser", "true");

		JSONObject userObject3 = new JSONObject();
		userObject3.put("user", userDetails3);

		//Add users to list
		JSONArray userList = new JSONArray();
		userList.add(userObject);
		userList.add(userDetails2);
		userList.add(userDetails3);

        //Write JSON file
        try (FileWriter file = new FileWriter("C:\\Users\\vinni\\IdeaProjects\\RestaurantWebApp\\users.json")) {

			file.write(userList.toJSONString());
			file.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public JSONArray ReadJSON()
	{
		@SuppressWarnings("unchecked")
			//JSON parser object to parse read file
			JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("C:\\Users\\vinni\\IdeaProjects\\RestaurantWebApp\\users.json"))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);

				JSONArray usersList = (JSONArray) obj;
				System.out.println(usersList);

				//Iterate over users array
				//usersList.forEach( user -> parseUserObject( (JSONObject) user , null) );

				return usersList;

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		return null;
	}

	private Boolean parseUserObject(JSONObject users,String email)
	{
		//Getting the users object within JSON Objects
		JSONObject userObject = (JSONObject) users.get("user");

		String firstName = (String) userObject.get("firstName");
		System.out.println(firstName);

		String lastName = (String) userObject.get("lastName");
		System.out.println(lastName);

		String emailJSON = (String) userObject.get("email");
		System.out.println(email);

		String premiumUser = (String) userObject.get("premiumuser");
		System.out.println(premiumUser);

		String semipremiumUser = (String) userObject.get("semipremiumuser");
		System.out.println(semipremiumUser);

		String nonpremiumUser = (String) userObject.get("nonpremiumuser");
		System.out.println(nonpremiumUser);

		if(email.equals(emailJSON) && premiumUser.equals("true")){
			emailPremium.put(email,"premium");
			return  true;
		}
		else if(email.equals(emailJSON) && semipremiumUser.equals("true")){
			emailPremium.put(email,"semipremium");
			return  true;
		}
		else if(email.equals(emailJSON) && nonpremiumUser.equals("true")){
			emailPremium.put(email,"nonpremium");
			return  true;
		}
		else {
			emailPremium.put(email, "nonpremium");
		}
		return true;
	}

	public void saveOrder(ShoppingCart cart) {

		final Session session = this.sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();

		User user = null;

		//Data Driven Programming
		//writePremiumUsers();
		JSONArray jsonarrayObj = ReadJSON();
		String email = cart.getCustomerInfo().getEmail();
		String premiumUserType = null;
		premiumUserType = checkEmailInPremiumUser(jsonarrayObj, email);

		if (this.getUser(cart.getCustomerInfo().getEmail()) == null) {
			user = new User();
			user.setfName(cart.getCustomerInfo().getfName());
			user.setlName(cart.getCustomerInfo().getlName());
			user.setEmailId(cart.getCustomerInfo().getEmail());
			user.setPhone(cart.getCustomerInfo().getPhone());
			user.setAddress(cart.getCustomerInfo().getAddress());
			session.persist(user);
		}else{
			user = this.getUser(cart.getCustomerInfo().getEmail());
		}

		Order order = new Order(user);
		if(premiumUserType.equals("premium")){
			user = this.getUser(cart.getCustomerInfo().getEmail());
			order.setOrderId(this.getMaxOrderNum()+1);
			double totalPrice = 0.0;
			order.setPrice(totalPrice); // 100 percent discount users
			order.setDate(new Timestamp(System.currentTimeMillis()));
		}
		else if (premiumUserType.equals("semipremium")){
			user = this.getUser(cart.getCustomerInfo().getEmail());
			order.setOrderId(this.getMaxOrderNum()+1);
			order.setPrice((cart.getTotalPrice())/2); // 50 percent discount users
			order.setDate(new Timestamp(System.currentTimeMillis()));
		}
		else if (premiumUserType.equals("nonpremium")){
			order.setOrderId(this.getMaxOrderNum() + 1);
			order.setPrice(cart.getTotalPrice()); //0 percent discount users
			order.setDate(new Timestamp(System.currentTimeMillis()));
		}
		session.persist(order);

        //Lambda Function - Functional Programming
		List<ItemInfo> infos = cart.getCartItem();
				infos.forEach(info-> {
				OrderItems items = new OrderItems(order);
				items.setQuantity(info.getQuantity());
				items.setName(info.getProductInfo().getProductName());
				items.setHotnessLevel(info.getHotnessLevel());
				session.persist(items);
		});

		/*for (ItemInfo info : cart.getCartItem()) {
			OrderItems items = new OrderItems(order);
			items.setQuantity(info.getQuantity());
			items.setName(info.getProductInfo().getProductName());
			items.setHotnessLevel(info.getHotnessLevel());
			session.persist(items);
		}*/
		transaction.commit();
		session.close();
		
		cart.setOrderNumber(order.getOrderId());
		
	}

	private String checkEmailInPremiumUser(JSONArray jsonarrayObj,String email) {
		//Lambda Function - Functional Programming
		jsonarrayObj.forEach( userObj -> parseUserObject( (JSONObject) userObj,email));
		String val = emailPremium.get(email);
		return val;
	}

	private User getUser(String emailId) {
		Session session = this.sessionFactory.openSession();
		String sql = "from User where emailId=:emailId";
		Query query = session.createQuery(sql);
		query.setParameter("emailId", emailId);
		Object val = (Object) query.uniqueResult();
		
		session.close();

		return (User) val;

	}

	private int getMaxOrderNum() {
		String sql = "Select max(o.orderId) from " + Order.class.getName() + " o ";
		Session session = sessionFactory.openSession();
		Query query = session.createQuery(sql);
		Integer value = (Integer) query.uniqueResult();
		if (value == null) {
			return 0;
		}
		session.close();
		return value;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public List<Order> showOrdersForToday() {

		String sql = "from Order where date>=:startDate and date<=:endDate";
		Date date = new Date(new Date().getTime()-24*3600*1000);
		Session session = sessionFactory.openSession();
		Query query = session.createQuery(sql);
		query.setTimestamp("startDate", new Timestamp(date.getTime()));
		query.setTimestamp("endDate", new Timestamp(System.currentTimeMillis()));
		List<Order> orders = query.list();
		session.close();
		return orders;

	}

	public Order getOrderDetails(int orderId){
		String sql = "from Order where orderId=:orderId";
		Session session = this.sessionFactory.openSession();
		Query query = session.createQuery(sql);
		query.setParameter("orderId", orderId);

		Order order = (Order) query.uniqueResult();
		session.close();
		return order;

	}
}
