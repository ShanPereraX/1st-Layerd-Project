package lk.ijse.studentsmanagement.controller;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import lk.ijse.studentsmanagement.dto.BatchDTO;
import lk.ijse.studentsmanagement.dto.CourseDTO;
import lk.ijse.studentsmanagement.service.ServiceFactory;
import lk.ijse.studentsmanagement.service.ServiceTypes;
import lk.ijse.studentsmanagement.service.custom.BatchService;
import lk.ijse.studentsmanagement.service.custom.CourseService;
import lk.ijse.studentsmanagement.dto.tblModels.BatchTM;
import lk.ijse.studentsmanagement.util.Navigation;
import lk.ijse.studentsmanagement.util.RegExPatterns;
import lk.ijse.studentsmanagement.util.Routes;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class AcademicAddNewBatchFormController implements Initializable {
    public AnchorPane pane;
    public Label lblId;
    public Label lblName;
    public Label lblDuration;
    public JFXDatePicker cmbDate;
    public ComboBox cmbCourseID;
    public JFXTextField txtBatchNo;
    public JFXTextField txtCrowd;
    public JFXTextField txtCourseFee;
    public TableView<BatchTM> tblOnGoingBatches;
    public TableColumn colBatchID;
    public TableColumn colFee;
    public TableColumn colCrowd;
    public TableColumn colStartDate;
    public Label lblBatchNo;
    public TableColumn colBatchNo;
    public Button btnAdd;
    CourseService courseService;
    BatchService batchService;
    @FXML
    private ComboBox<String> cmbCourse;

    @FXML
    void btnAddOnAction(ActionEvent event) {
        try {
            BatchDTO batchDTO = addNewBatch();
            if (batchDTO != null) {
                new Alert(Alert.AlertType.INFORMATION, "Added!").show();
                txtCourseFee.clear();
                txtCrowd.clear();
                loadCoursesList();
            }
        } catch (SQLException | ClassNotFoundException | RuntimeException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
    }

    private BatchDTO addNewBatch() throws SQLException, ClassNotFoundException, RuntimeException {
        if (!cmbCourse.getValue().isEmpty()) {
            if (RegExPatterns.getDoublePattern().matcher(txtCourseFee.getText()).matches()) {
                if (cmbDate.getValue() != null) {
                    if (Integer.parseInt(txtCrowd.getText()) > 0) {
                        return batchService.save(new BatchDTO(lblBatchNo.getText() + cmbCourse.getValue(), Integer.parseInt(lblBatchNo.getText()), cmbCourse.getValue(), Double.parseDouble(txtCourseFee.getText()), Date.valueOf(cmbDate.getValue()), Integer.parseInt(txtCrowd.getText())));
                    } else {
                        new Alert(Alert.AlertType.ERROR, "Enter Crowd!").show();
                    }
                } else {
                    new Alert(Alert.AlertType.ERROR, "Select a date first!").show();
                }
            } else {
                txtCourseFee.setFocusColor(Color.RED);
                new Alert(Alert.AlertType.ERROR, "Enter Fee Correctly").show();
            }
        } else {
            new Alert(Alert.AlertType.INFORMATION, "Please Select A Course First!").show();
        }
        throw new RuntimeException("Batch not added...");
    }

    @FXML
    void btnBackOnAction(ActionEvent event) throws IOException {
        Navigation.navigate(Routes.ACADEMIC_MANAGE_BATCHES, pane);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnAdd.setDisable(true);
        try {
            courseService = ServiceFactory.getInstance().getService(ServiceTypes.COURSE);
            batchService = ServiceFactory.getInstance().getService(ServiceTypes.BATCH);
            loadCoursesList();
            colBatchID.setCellValueFactory(new PropertyValueFactory<>("id"));
            colFee.setCellValueFactory(new PropertyValueFactory<>("fee"));
            colStartDate.setCellValueFactory(new PropertyValueFactory<>("starting_date"));
            colCrowd.setCellValueFactory(new PropertyValueFactory<>("maxStdCount"));
            colBatchNo.setCellValueFactory(new PropertyValueFactory<>("batchNo"));
        } catch (SQLException | ClassNotFoundException | RuntimeException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    public void loadCoursesList() throws SQLException, ClassNotFoundException {
        List<CourseDTO> courseArrayList = courseService.getCourseList();
        ObservableList<String> observableArrayList = FXCollections.observableArrayList();
        for (CourseDTO ele : courseArrayList) observableArrayList.add(ele.getId());
        cmbCourse.setItems(observableArrayList);
    }

    public void cmbCourseOnAction(ActionEvent actionEvent) {
        try {
            btnAdd.setDisable(false);
            if (cmbCourse.getSelectionModel().getSelectedItem() != null) {
                CourseDTO courseDTO = courseService.view(new CourseDTO(cmbCourse.getValue()));
                lblName.setText(courseDTO.getName());
                lblDuration.setText(courseDTO.getDuration());
                lblId.setText(courseDTO.getId());
                setBatchNo(cmbCourse.getValue());
                setBatchTable(cmbCourse.getValue());
            }
        } catch (SQLException | ClassNotFoundException | RuntimeException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
    }

    void setBatchNo(String course) throws SQLException, ClassNotFoundException, RuntimeException {
        BatchDTO batchDTO = batchService.getLastBatchNo(course);
        if (batchDTO.getBatchNo() == 0) {
            lblBatchNo.setText(String.valueOf(1));
        } else {
            int batchNo = batchDTO.getBatchNo();
            batchNo++;
            lblBatchNo.setText(String.valueOf(batchNo));
        }
    }

    public void setBatchTable(String course) throws SQLException, ClassNotFoundException, RuntimeException {
        //load batch table in specific course
        List<BatchDTO> batchesArrayList = batchService.getBatches(course);
        ObservableList<BatchTM> observableListBatchTM = FXCollections.observableArrayList();
        for (BatchDTO ele : batchesArrayList) {
            observableListBatchTM.add(new BatchTM(ele.getId(), ele.getBatchNo(), ele.getFee(), ele.getStarting_date(), ele.getMaxStdCount()));
        }
        tblOnGoingBatches.setItems(observableListBatchTM);
    }
}
