package com.networknt.tram.todolist.command;



import com.networknt.service.SingletonServiceFactory;
import com.networknt.tram.todolist.common.Utils;
import org.h2.tools.RunScript;
import org.junit.Assert;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;


public class CommandModuleTest {
  public static DataSource ds;

  static {
    ds = SingletonServiceFactory.getBean(DataSource.class);
    try (Connection connection = ds.getConnection()) {
      // Runscript doesn't work need to execute batch here.
      String schemaResourceName = "/todolist-example-h2-ddl.sql";
      InputStream in = CommandModuleTest.class.getResourceAsStream(schemaResourceName);

      if (in == null) {
        throw new RuntimeException("Failed to load resource: " + schemaResourceName);
      }
      InputStreamReader reader = new InputStreamReader(in);
      RunScript.execute(connection, reader);

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private TodoCommandService todoCommandService = SingletonServiceFactory.getBean(TodoCommandService.class);

  @Test
  public void testCreate() {
    String title = Utils.generateUniqueString();
    String id = todoCommandService.create(new CreateTodoRequest(title, false, 0)).getId();
    Todo todo = todoCommandService.findOne(id);
    Assert.assertNotNull(todo);
    Assert.assertEquals(title, todo.getTitle());
  }

  @Test
  public void testUpdate() throws TodoNotFoundException {
    Todo todo = todoCommandService.create(new CreateTodoRequest(Utils.generateUniqueString(), false, 9));
    String title = Utils.generateUniqueString();
    todoCommandService.update(todo.getId(), new UpdateTodoRequest(title, false, 0));
    todo = todoCommandService.findOne(todo.getId());
    Assert.assertNotNull(todo);
    Assert.assertEquals(title, todo.getTitle());
  }

  @Test
  public void testDelete() {
    Todo todo = todoCommandService.create(new CreateTodoRequest(Utils.generateUniqueString(), false, 9));
    todoCommandService.delete(todo.getId());
    Assert.assertNull(todoCommandService.findOne(todo.getId()));
  }
}
