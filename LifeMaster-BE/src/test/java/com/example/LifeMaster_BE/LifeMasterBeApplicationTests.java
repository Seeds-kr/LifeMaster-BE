package com.example.LifeMaster_BE;

import com.example.LifeMaster_BE.FunctionManager.ToDoList.TodoEntity;
import com.example.LifeMaster_BE.FunctionManager.ToDoList.TodoRepository;
import com.example.LifeMaster_BE.FunctionManager.ToDoList.TodoService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
@SpringBootTest
class LifeMasterBeApplicationTests {

	@Test
	void contextLoads() {
	}

}

class TodoServiceTest {

	@InjectMocks
	private TodoService todoService;

	@Mock
	private TodoRepository todoRepository;

	private TodoEntity todo;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		todo = new TodoEntity();
		todo.setId(1L);
		todo.setTitle("Test Todo");
		todo.setDescription("This is a test todo.");
	}

	@Test
	public void findAll_ShouldReturnListOfTodos() {
		List<TodoEntity> todos = new ArrayList<>();
		todos.add(todo);

		when(todoRepository.findAll()).thenReturn(todos);

		List<TodoEntity> result = todoService.findAll();

		assertEquals(1, result.size());
		assertEquals("Test Todo", result.get(0).getTitle());
	}

	@Test
	public void save_ShouldReturnSavedTodo() {
		when(todoRepository.save(any(TodoEntity.class))).thenReturn(todo);

		TodoEntity result = todoService.save(todo);

		assertNotNull(result);
		assertEquals("Test Todo", result.getTitle());
	}

	@Test
	public void findById_ShouldReturnTodo_WhenExists() {
		when(todoRepository.findById(anyLong())).thenReturn(Optional.of(todo));

		Optional<TodoEntity> result = todoService.findById(1L);

		assertTrue(result.isPresent());
		assertEquals("Test Todo", result.get().getTitle());
	}

	@Test
	public void findById_ShouldReturnEmpty_WhenNotExists() {
		when(todoRepository.findById(anyLong())).thenReturn(Optional.empty());

		Optional<TodoEntity> result = todoService.findById(2L);

		assertFalse(result.isPresent());
	}

	@Test
	public void deleteById_ShouldReturnTrue_WhenExists() {
		when(todoRepository.existsById(anyLong())).thenReturn(true);

		boolean result = todoService.deleteById(1L);

		assertTrue(result);
		verify(todoRepository, times(1)).deleteById(1L);
	}

	@Test
	public void deleteById_ShouldReturnFalse_WhenNotExists() {
		when(todoRepository.existsById(anyLong())).thenReturn(false);

		boolean result = todoService.deleteById(2L);

		assertFalse(result);
		verify(todoRepository, never()).deleteById(anyLong());
	}

	@Test
	public void update_ShouldReturnUpdatedTodo_WhenExists() {
		when(todoRepository.existsById(anyLong())).thenReturn(true);
		when(todoRepository.save(any(TodoEntity.class))).thenReturn(todo);

		Optional<TodoEntity> result = todoService.update(1L, todo);

		assertTrue(result.isPresent());
		assertEquals("Test Todo", result.get().getTitle());
	}

	@Test
	public void update_ShouldReturnEmpty_WhenNotExists() {
		when(todoRepository.existsById(anyLong())).thenReturn(false);

		Optional<TodoEntity> result = todoService.update(2L, todo);

		assertFalse(result.isPresent());
	}
}
