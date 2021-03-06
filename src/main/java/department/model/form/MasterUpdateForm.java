package department.model.form;

import java.util.Date;

import lombok.Data;

@Data
public class MasterUpdateForm {
	private Integer id;
    private String name;
    private String phone;
	
	private String topic;
    private Date startDate;
    private Date endDate;
    private Integer teacher;
    private Integer department;
}
