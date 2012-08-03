package com.superdownloader.proeasy.core.persistence;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.superdownloader.proeasy.core.type.DownloadQueueItem;

/**
 * @author harley
 *
 */
@Repository
public class HibernateDownloadsQueueDao implements DownloadsQueueDao {

	@Autowired
	private SessionFactory sessionFactory;

	protected final Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

	@Override
	@Transactional
	public void push(DownloadQueueItem item) {
		getCurrentSession().save(item);
	}

	@Override
	@Transactional
	public void repush(DownloadQueueItem item) {
		DownloadQueueItem itemdb = (DownloadQueueItem) getCurrentSession()
				.get(DownloadQueueItem.class, item.getId());
		itemdb.setInProgress(false);
		getCurrentSession().save(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public List<DownloadQueueItem> pop(int maxDownloadPerUser) {
		Query query = getCurrentSession().createQuery("from DownloadQueueItem d where " +
				"(select count(*) from DownloadQueueItem f where " +
				"f.user.id = d.user.id AND f.id < d.id) <= :maxDownloadPerUser");
		query.setParameter("maxDownloadPerUser", maxDownloadPerUser);
		return query.list();
	}

	@Override
	@Transactional
	public void setInProgress(List<Long> idsToUpdate) {
		Query query = getCurrentSession().createQuery("update DownloadQueueItem " +
				"set inProgress = true where id IN (:ids)");
		query.setParameter("ids", idsToUpdate);
		query.executeUpdate();
	}

	@Override
	@Transactional
	public void remove(DownloadQueueItem item) {
		Query query = getCurrentSession().createQuery("delete from DownloadQueueItem where id = :id");
		query.setParameter("id", item.getId());
		query.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public List<DownloadQueueItem> queue(long userId) {
		Query query = getCurrentSession().createQuery("from DownloadQueueItem d " +
				"where d.user.id = :userId and d.inProgress = false");
		query.setParameter("userId", userId);
		return query.list();
	}

	@Override
	@Transactional
	public DownloadQueueItem get(long userId, long downloadId) {
		Query query = getCurrentSession().createQuery("from DownloadQueueItem d " +
				"where d.id = :downloadId and d.user.id = :userId");
		query.setParameter("downloadId", downloadId);
		query.setParameter("userId", userId);
		return (DownloadQueueItem) query.uniqueResult();
	}

}
