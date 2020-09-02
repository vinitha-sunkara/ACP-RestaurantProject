package com.taj.validator;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.taj.model.HotnessInfo;
import com.taj.model.ItemInfo;
import com.taj.model.ShoppingCart;
import com.taj.utils.TajUtils;

@Component
public class HotnessValidator implements Validator {

	public boolean supports(Class<?> arg0) {
		return arg0 == HotnessInfo.class;
	}

	@Autowired(required = true)
	private HttpServletRequest request;

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public void validate(Object arg0, Errors errors) {

		HotnessInfo hotnessInfo = (HotnessInfo) arg0;

		ShoppingCart cart = TajUtils.getOrderFromSession(request);

		/*if (hotnessInfo.getHotnessLevel() == null){// && TajUtils.categoriesForHotnessLevel().contains(cart.getCartItem().get(0).getProductInfo().getCategoryId()) ) {
			errors.rejectValue("hotnessLevel[0]", "NotEmpty.hotness.value");
		} else {
			List<String> a = hotnessInfo.getHotnessLevel();
			
			if (a != null && cart != null) {
				int i=0;
				for (ItemInfo info:cart.getCartItem()) {
					try {
						if (TajUtils.categoriesForHotnessLevel().contains(info.getProductInfo().getCategoryId()) && a.get(i) == null) {
							errors.rejectValue("hotnessLevel[" + i + "]", "NotEmpty.hotness.value");
						}
							
					} catch (IndexOutOfBoundsException e) {
						errors.rejectValue("hotnessLevel[" + i + "]", "NotEmpty.hotness.value");
					}
					i++;

				}
			}

		}*/
		
		
		int count=0;
		// A Higher Order Function
		List<String> hotlist = hotnessInfo.getHotnessLevel();
		Collections.sort(hotlist, (String a, String b) -> {
			return a.compareTo(b);
		});
		System.out.println(hotlist);

		//Reverse Order Comparator
		Comparator<String> comparator = (String a, String b) -> {
			return a.compareTo(b);
		};

		Comparator<String> comparatorReversed = comparator.reversed();
		Collections.sort(hotlist, comparatorReversed);
		System.out.println(hotlist);
		
		for(ItemInfo info:cart.getCartItem()){
			
			if(TajUtils.categoriesForHotnessLevel().contains(info.getProductInfo().getCategoryId())){
				try{
					if(hotlist==null){
						errors.rejectValue("hotnessLevel[" + count + "]", "NotEmpty.hotness.value");
						break;
					}else if(hotlist.get(count)==null){
						errors.rejectValue("hotnessLevel[" + count + "]", "NotEmpty.hotness.value");
						break;
					}else{
						count++;
					}
				}catch (IndexOutOfBoundsException e) {
					errors.rejectValue("hotnessLevel[" + count + "]", "NotEmpty.hotness.value");
				}
				
				
			}
			
		}
		

	}

}
