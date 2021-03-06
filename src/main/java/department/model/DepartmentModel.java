/**
 * 
 */
package department.model;

import department.dao.IDepartmentDAO;
import department.dao.ITeacherDAO;
import department.model.bo.Department;
import department.model.form.DepartmentCreateForm;
import department.model.form.DepartmentUpdateForm;
import department.model.mapper.DepartmentMapper;
import department.ui.controller.model.DepartmentViewModel;
import department.ui.utils.FxSchedulers;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * @author Nikolay
 *
 */
@Repository
public class DepartmentModel implements IDepartmentModel {

	@Autowired
	IDepartmentDAO departmentDao;
	
	@Autowired
	ITeacherDAO teacherDao;

	@Override
	public Observable<? extends DepartmentViewModel> fetch(
			@NotNull(message = "id cannot be null") int id) {

		return Observable.defer(() -> Observable.create((Observable.OnSubscribe<? extends Department>) sub -> {

			sub.onStart();
			try {
				sub.onNext(
						departmentDao.find(id,true));
			} catch (Exception e) {
				sub.onError(e);
			} finally {
				sub.onCompleted();
			}
		})).observeOn(FxSchedulers.platform()).subscribeOn(Schedulers.newThread()).map(DepartmentMapper::toViewModel);
	}
	
	@Override
	public Observable<? extends DepartmentViewModel> fetchDepartmentByPaperId(@Min(0) int paperId) {
		return Observable
				.defer(() -> Observable.create((Observable.OnSubscribe<? extends Department>) sub -> {

					sub.onStart();
					try {
						val list=teacherDao.getChiefTeachersByPaperId(paperId,true);
						val cht = !list.isEmpty()?list.get(0):null;
						sub.onNext(cht==null?null:departmentDao.find(cht.getDepartment().getId()));						
						
					} catch (Exception e) {
						sub.onError(e);
					} finally {
						sub.onCompleted();
					}
				})).observeOn(FxSchedulers.platform()).subscribeOn(Schedulers.newThread()).map(DepartmentMapper::toViewModel);
	}
	@Override
	public Observable<? extends DepartmentViewModel> fetchDepartmentByTopicId(@Min(0) int topicId) {
		return Observable
				.defer(() -> Observable.create((Observable.OnSubscribe<? extends Department>) sub -> {

					sub.onStart();
					try {
						val list=teacherDao.getTeachersByTopicId(topicId,true);
						val cht = !list.isEmpty()?list.get(0):null;
						sub.onNext(cht==null?null:departmentDao.find(cht.getDepartment().getId()));						
						
					} catch (Exception e) {
						sub.onError(e);
					} finally {
						sub.onCompleted();
					}
				})).observeOn(FxSchedulers.platform()).subscribeOn(Schedulers.newThread()).map(DepartmentMapper::toViewModel);
	}
	@Override
	public Observable<Collection<? extends DepartmentViewModel>> fetchDepartments(@Min(0) long offset,
			@Min(0) long limit) {
		return Observable
				.defer(() -> Observable.create((Observable.OnSubscribe<Collection<? extends Department>>) sub -> {

					sub.onStart();
					try {
						sub.onNext(departmentDao.findAll("",limit, offset,true));
					} catch (Exception e) {
						sub.onError(e);
					} finally {
						sub.onCompleted();
					}
				})).observeOn(FxSchedulers.platform()).subscribeOn(Schedulers.newThread()).map(DepartmentMapper::toViewModel);
	}

	@Override
	public Observable<Collection<? extends DepartmentViewModel>> fetchDepartments(
			@NotNull(message = "query cannot be null") String query, @Min(0) long offset, @Min(0) long limit) {
		return Observable
				.defer(() -> Observable.create((Observable.OnSubscribe<Collection<? extends Department>>) sub -> {

					sub.onStart();
					try {
						sub.onNext(departmentDao.findAll(query,limit, offset,true));
					} catch (Exception e) {
						sub.onError(e);
					} finally {
						sub.onCompleted();
					}
				})).observeOn(FxSchedulers.platform()).subscribeOn(Schedulers.newThread()).map(DepartmentMapper::toViewModel);
	}

	@Override
	public Observable<? extends DepartmentViewModel> create(
			@NotNull(message = "form cannot be null") DepartmentCreateForm form) {

		return Observable.defer(() -> Observable.create((Observable.OnSubscribe<? extends Department>) sub -> {

			sub.onStart();
			try {
				sub.onNext(
						departmentDao.insert(Department.builder().phone(form.getPhone()).name(form.getName()).build()));
			} catch (Exception e) {
				sub.onError(e);
			} finally {
				sub.onCompleted();
			}
		})).observeOn(FxSchedulers.platform()).subscribeOn(Schedulers.newThread()).map(DepartmentMapper::toViewModel);
	}

	@Override
	public void update(DepartmentUpdateForm form, DepartmentViewModel model, Action1<? super Throwable> errCallback) {
		Observable.defer(() -> Observable.create((Observable.OnSubscribe<? extends Department>) sub -> {

			sub.onStart();
			try {
				val department = Department.builder().id(form.getId()).phone(form.getPhone()).name(form.getName())
						.build();
				departmentDao.update(department);
				sub.onNext(departmentDao.find(department.getId(),true));
			} catch (Exception e) {
				sub.onError(e);
			} finally {
				sub.onCompleted();
			}
		})).observeOn(FxSchedulers.platform()).subscribeOn(Schedulers.newThread()).subscribe(result -> {
			model.setName(result.getName());
			model.setPhone(result.getPhone());
		} , errCallback::call);

	}

	@Override
	public Observable<? extends Integer> count() {
		return Observable.defer(() -> Observable.create((Observable.OnSubscribe<? extends Integer>) sub -> {

			sub.onStart();
			try {
				sub.onNext(departmentDao.count());
			} catch (Exception e) {
				sub.onError(e);
			} finally {
				sub.onCompleted();
			}
		})).observeOn(FxSchedulers.platform()).subscribeOn(Schedulers.newThread());
	}

}
