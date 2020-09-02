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

	public void writePremiumUsers(){
		//First User
		JSONObject userDetails = new JSONObject();
		userDetails.put("firstName", "Lokesh");
		userDetails.put("lastName", "Gupta");
		userDetails.put("email", "abc@gmail.com");

		JSONObject userObject = new JSONObject();
		userObject.put("user", userDetails);

		//Second User
		JSONObject userDetails2 = new JSONObject();
		userDetails2.put("firstName", "Brian");
		userDetails2.put("lastName", "Schultz");
		userDetails2.put("email", "def@gmail.com");

		JSONObject userObject2 = new JSONObject();
		userObject2.put("user", userDetails2);

		//Add employees to list
		JSONArray userList = new JSONArray();
		userList.add(userObject);
		userList.add(userDetails2);

		//Write JSON file
		try (FileWriter file = new FileWriter("users.json")) {

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

			try (FileReader reader = new FileReader("users.json"))
			{
				//Read JSON file
				Object obj = jsonParser.parse(reader);

				JSONArray usersList = (JSONArray) obj;
				System.out.println(usersList);

				//Iterate over users array
				usersList.forEach( user -> parseEmployeeObject( (JSONObject) user , null) );

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

	private static Boolean parseEmployeeObject(JSONObject employee,String email)
	{
		//Get employee object within list
		JSONObject userObject = (JSONObject) employee.get("user");

		//Get employee first name
		String firstName = (String) userObject.get("firstName");
		System.out.println(firstName);

		//Get employee last name
		String lastName = (String) userObject.get("lastName");
		System.out.println(lastName);

		//Get employee website name
		String emailJSON = (String) userObject.get("email");
		System.out.println(email);

		if(email.equals(emailJSON)){
			return  true;
		}
		else{
			return  false;
		}
	}

	public void saveOrder(ShoppingCart cart) {

		final Session session = this.sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();

		User user = null;

		//Data Driven Programming
		writePremiumUsers();
		JSONArray jsonarrayObj = ReadJSON();
		String email = cart.getCustomerInfo().getEmail();
		Boolean premiumUser = checkEmailInPremiumUser(jsonarrayObj, email);

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
		if(premiumUser){
			user = this.getUser(cart.getCustomerInfo().getEmail());
			order.setOrderId(this.getMaxOrderNum()+1);
			double totalPrice = 0.0;
			order.setPrice(totalPrice);
			order.setDate(new Timestamp(System.currentTimeMillis()));
		}
		else {
			order.setOrderId(this.getMaxOrderNum() + 1);
			order.setPrice(cart.getTotalPrice());
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

	private Boolean checkEmailInPremiumUser(JSONArray jsonarrayObj,String email) {
		Boolean val = false;
		//jsonarrayObj.forEach( userObj -> parseEmployeeObject( (JSONObject) userObj,email));
		JSONArray jsonArray = (JSONArray) jsonarrayObj.get(1);
		for (int i=0;i<jsonArray.size();i++){
			 val = parseEmployeeObject( (JSONObject) jsonArray.get(i),email);
		}
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
