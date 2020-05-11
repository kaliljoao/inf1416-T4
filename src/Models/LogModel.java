package Models;

import java.sql.Timestamp;
import java.util.Date;

public class LogModel {
    private Date datetime;
    private int id;
    private String user;
    private String file;
    private String message;

    public LogModel(int id, String message, String user, String file, Date datetime) {
        this.datetime = datetime;
        this.id = id;
        this.user = user;
        this.file = file;
        this.message = message;
        if (user != null) this.message = this.message.replace("<login_name>", user);
        if (file != null) this.message = this.message.replace("<arq_name>", file);
    }

    public Date getDatetime() {
        return datetime;
    }

    public int getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public String getFile() {
        return file;
    }

    public String getMessage() {
        return message;
    }
}
