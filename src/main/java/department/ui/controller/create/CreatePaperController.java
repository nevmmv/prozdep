package department.ui.controller.create;

import department.model.IMasterModel;
import department.model.IPaperModel;
import department.model.IPostgraduateModel;
import department.model.ITeacherModel;
import department.model.form.PaperCreateForm;
import department.ui.controller.model.MasterViewModel;
import department.ui.controller.model.PostgraduateViewModel;
import department.ui.controller.model.TeacherViewModel;
import department.ui.utils.DefaultStringConverter;
import department.ui.utils.UiConstants;
import department.ui.utils.UiUtils;
import department.utils.RxUtils;
import department.utils.TextUtils;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.extern.java.Log;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import rx.Observable;
import rx.functions.Func2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.logging.Level;

/**
 * Created by Максим on 2/19/2017.
 */
@Log
@Controller
public final class CreatePaperController {

    @FXML
    private Parent viewRoot;
    @FXML
    private TextField titleField;
    @FXML
    private TextField typeField;
    @FXML
    private TextField yearField;
    @FXML
    private ComboBox<TeacherViewModel> supervisorBox;
    @FXML
    private ComboBox<Executor> executorBox;

    private final IPaperModel paperModel;
    private final IMasterModel masterModel;
    private final IPostgraduateModel postgraduateModel;
    private final ITeacherModel teacherModel;

    @Autowired
    public CreatePaperController(IPaperModel paperModel, IMasterModel masterModel, IPostgraduateModel postgraduateModel,
                                 ITeacherModel teacherModel) {
        this.paperModel = paperModel;
        this.masterModel = masterModel;
        this.postgraduateModel = postgraduateModel;
        this.teacherModel = teacherModel;
    }

    private static final class Executor {

        private final int id;
        private final String message;

        Executor(int id, String message) {
            this.id = id;
            this.message = message;
        }

        Executor(MasterViewModel m, String message) {
            this(m.getId(), message);
        }

        Executor(PostgraduateViewModel m, String message) {
            this(m.getId(), message);
        }

    }

    @FXML
    private void initialize() {

        supervisorBox.setConverter(new DefaultStringConverter<TeacherViewModel>() {
            @Override
            public String toString(TeacherViewModel object) {
                return object == null ? "" : object.getFirstName();
            }
        });

        executorBox.setConverter(new DefaultStringConverter<Executor>() {
            @Override
            public String toString(Executor object) {
                return object == null ? "" : object.message;
            }
        });

        supervisorBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {

            if (!TextUtils.isEmpty(newValue) && newValue.length() >= 3) {
                teacherModel.fetchTeachers(newValue, 0, UiConstants.HINT_RESULT)
                        .doOnCompleted(supervisorBox::show)
                        .subscribe(supervisorBox.getItems()::setAll,
                                th -> {
                                    UiUtils.createErrDialog("Не вдалося завантажити список викладачів").showAndWait();
                                    log.log(Level.WARNING, "Failed to fetch teachers");
                                });
            }
        });

        executorBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {

            if (!TextUtils.isEmpty(newValue) && newValue.length() >= 3) {

                Observable.zip(masterModel.fetchMasters(newValue, 0, UiConstants.HINT_RESULT),
                        postgraduateModel.fetchPostgraduates(newValue, 0, UiConstants.HINT_RESULT),

                        (Func2<Collection<? extends MasterViewModel>, Collection<? extends PostgraduateViewModel>, Collection<Executor>>) (masterViewModels, postgraduateViewModels) -> {
                            val result = new ArrayList<Executor>(masterViewModels.size() + postgraduateViewModels.size());

                            for (val m : masterViewModels) result.add(new Executor(m, m.getFirstName()));
                            for (val m : postgraduateViewModels) result.add(new Executor(m, m.getFirstName()));
                            return result;
                        })
                        .doOnCompleted(executorBox::show)
                        .subscribe(executorBox.getItems()::setAll,
                                th -> {
                                    UiUtils.createErrDialog("Не вдалося завантажити список викладачів").showAndWait();
                                    log.log(Level.WARNING, "Failed to fetch teachers");
                                });
            }
        });
    }

    @FXML
    private void onCreatePaper() {

        val year = tryGetYear(yearField.getText());

        if (year < 0) {
            UiUtils.createWarnDialog("Неправильно вказаний рік").showAndWait();
            yearField.requestFocus();
            return;
        }

        val name = titleField.getText();

        if (TextUtils.isEmpty(name)) {
            UiUtils.createWarnDialog("Назву не вказано").showAndWait();
            titleField.requestFocus();
            return;
        }

        val type = typeField.getText();

        if (TextUtils.isEmpty(type)) {
            UiUtils.createWarnDialog("Тип не вказано").showAndWait();
            typeField.requestFocus();
            return;
        }

        val supervisor = supervisorBox.valueProperty().get();

        if (supervisor == null) {
            UiUtils.createWarnDialog("Для того, аби продовжити, виберіть викладача зі списку").showAndWait();
            supervisorBox.requestFocus();
            return;
        }

        val executor = executorBox.valueProperty().get();

        if (executor == null) {
            UiUtils.createWarnDialog("Для того, аби продовжити, виберіть науковця зі списку").showAndWait();
            executorBox.requestFocus();
            return;
        }

        val form = new PaperCreateForm();

        form.setType(type);
        form.setName(name);
        form.setYear(year);
        form.setExecutor(executor.id);
        form.setSupervisor(supervisor.getId());
        paperModel.create(form).subscribe(model -> {
            log.log(Level.INFO, "Model created");

            if (viewRoot.getScene() == null) {
                RxUtils.fromProperty(viewRoot.sceneProperty())
                        .filter(scene -> scene != null)
                        .flatMap(scene -> RxUtils.fromProperty(scene.windowProperty()))
                        .filter(window -> window != null && window instanceof Stage)
                        .take(1)
                        .map(window -> (Stage) window)
                        .subscribe(Stage::close);
            } else {
                ((Stage) viewRoot.getScene().getWindow()).close();
            }
        }, th -> {
            log.log(Level.SEVERE, "Failed to create model", th);
            UiUtils.createErrDialog("Не вдалося створити наукову роботу").showAndWait();
        });
    }

    private int tryGetYear(String str) {

        try {
            val year = Integer.parseInt(str);
            return year >= 1900 && year <= Calendar.getInstance().get(Calendar.YEAR) ? year : -1;
        } catch (Exception e) {
            return -1;
        }
    }

}
