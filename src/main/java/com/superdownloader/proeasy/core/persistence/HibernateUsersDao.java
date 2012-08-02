/**
 * 
 */
package com.superdownloader.proeasy.core.persistence;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.superdownloader.proeasy.core.type.User;

/**
 * @author harley
 *
 */
@Repository
public class HibernateUsersDao implements UsersDao {

	@Autowired
	private SessionFactory sessionFactory;

	protected final Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

	@Override
	public boolean isValidUser(String username, String password) {
		Query query = getCurrentSession().createQuery("select 1 from User where username = :username and password = MD5(:password)");
		query.setParameter("username", username);
		query.setParameter("password", password);
		Integer result = (Integer) query.uniqueResult();
		return (result != null && result == 1);
	}

	@Override
	public void save(User user) {
		getCurrentSession().save(user);
	}

	@Override
	public User get(int userId) {
		return (User) getCurrentSession().get(User.class, userId);
	}

	@Override
	public User get(String username) {
		Query query = getCurrentSession().createQuery("from User where username = :username");
		query.setParameter("username", username);
		return (User) query.uniqueResult();
	}

}