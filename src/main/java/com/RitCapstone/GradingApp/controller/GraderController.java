package com.RitCapstone.GradingApp.controller;

import java.util.Map;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.RitCapstone.GradingApp.GradeHomework;
import com.RitCapstone.GradingApp.HomeworkOptions;
import com.RitCapstone.GradingApp.ProfessorAndGrader;
import com.RitCapstone.GradingApp.service.GradeHomeworkService;
import com.RitCapstone.GradingApp.service.HomeworkOptionsService;
import com.RitCapstone.GradingApp.service.SubmissionDBService;
import com.RitCapstone.GradingApp.validator.AuthenticationValidator;

@Controller
@RequestMapping("/grader")
@SessionAttributes("gradeHomework")
public class GraderController {

	private static Logger log = Logger.getLogger(GraderController.class);

	@Autowired
	AuthenticationValidator authValidator;

	@Autowired
	HomeworkOptionsService homeworkOptionsService;

	@Autowired
	GradeHomeworkService gradeHomeworkService;

	@Autowired
	SubmissionDBService submissionDBService;

	/**
	 * This method will trim all the strings received from form data
	 */
	@InitBinder
	public void stringTrimmer(WebDataBinder dataBinder) {

		StringTrimmerEditor trimmer = new StringTrimmerEditor(true);
		// If after trimming the string is empty, it is converted to null {Set by true}

		dataBinder.registerCustomEditor(String.class, trimmer);
	}

	@GetMapping("/authenticate")
	public String showFormForAuthentication(Model model) {
		String jspToDisplay = "graderView/grader-authenticate";
		String log_prepend = "[GET /authenticate  (grader)]";

		if (!model.containsAttribute("grader")) {
			model.addAttribute("grader", new ProfessorAndGrader());
			log.debug(log_prepend + "adding grader to model");
		}

		log.debug(log_prepend + "model: " + model);
		log.debug(log_prepend + "Displaying " + jspToDisplay);
		return jspToDisplay;
	}

	@PostMapping("/authenticate")
	public String authenticateGrader(@Valid @ModelAttribute("grader") ProfessorAndGrader grader,
			BindingResult bindingResult, RedirectAttributes redirectAttributes) {

		String log_prepend = "[POST /authenticate (grader)]";

		authValidator.setUser(AuthenticationValidator.graderString);
		authValidator.validate(grader, bindingResult);

		if (bindingResult.hasErrors()) {

			redirectAttributes.addFlashAttribute("grader", grader);
			redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.grader", bindingResult);

			log.debug(log_prepend + "Redirecting to /authenticate");
			return "redirect:authenticate";
		}
		log.debug(log_prepend + " Login successful, Redirecting to /showForm");
		return "redirect:showForm";

	}

	@GetMapping("/showForm")
	public String showForm(Map<String, Object> model) {
		String jspToDisplay = "graderView/grade-homework-options";
		String log_prepend = "[GET /showForm]";

		if (!model.containsKey("gradeHomework")) {
			model.put("gradeHomework", new GradeHomework());
			log.debug(log_prepend + "adding gradeHomework object to model");

		}

		if (!model.containsKey("hw")) {
			HomeworkOptions hwOptions = new HomeworkOptions();
			hwOptions.setHomeworkOptions(homeworkOptionsService.getHomeworkOptions());
			model.put("hw", hwOptions);

			log.debug(log_prepend + "adding hw to model:" + hwOptions.getHomeworkOptions());

		}

		log.debug(log_prepend + "Displaying " + jspToDisplay);

		return jspToDisplay;
	}

	@PostMapping(value = "/showForm", params = { "listByStudent" })
	public String validateShowFormStudentList(@Valid @ModelAttribute("gradeHomework") GradeHomework gradeHwObject,
			BindingResult bindingResult, RedirectAttributes redirectAttribute) {

		String log_prepend = "[POST /showForm (listByStudent)]";
		boolean isCorrect = checkValidationForShowForm(log_prepend, gradeHwObject, bindingResult, redirectAttribute);

		if (!isCorrect) {
			return "redirect:showForm";
		} else {
			// redirect to page where students are listed
			return "redirect:showStudentList";
		}

	}

	@PostMapping(value = "/showForm", params = { "listByQuestion" })
	public String validateShowFormQuestionList(@Valid @ModelAttribute("gradeHomework") GradeHomework gradeHwObject,
			BindingResult bindingResult, RedirectAttributes redirectAttribute) {

		String log_prepend = "[POST /showForm (listByQuestion)]";
		boolean isCorrect = checkValidationForShowForm(log_prepend, gradeHwObject, bindingResult, redirectAttribute);

		if (!isCorrect) {
			return "redirect:showForm";
		} else {
			// TODO redirect to page where questions are listed
			return "redirect:showQuestionList";
		}

	}

	private boolean checkValidationForShowForm(String log_prepend, GradeHomework gradeHwObject,
			BindingResult bindingResult, RedirectAttributes redirectAttribute) {

		log.debug("In " + log_prepend);

		if (bindingResult.hasErrors()) {
			redirectAttribute.addFlashAttribute("gradeHomework", gradeHwObject);
			redirectAttribute.addFlashAttribute("org.springframework.validation.BindingResult.gradeHomework",
					bindingResult);
			log.error("homework section for GradeHomework not selected");
			return false;
		}
		log.debug("homework section for GradeHomework selected");
		return true;
	}

	@GetMapping("/showStudentList")
	public String showStudentList(Map<String, Object> model,
			@SessionAttribute("gradeHomework") GradeHomework gradeHomework) {

		String log_prepend = "[GET /showStudentList]";
		String jspToDisplay = "graderView/list-hw-by-student";
		log.debug(log_prepend + " Displaying " + jspToDisplay);
		model.put("studentList", gradeHomeworkService.getListOfStudents(gradeHomework.getHomework()));
		return jspToDisplay;
	}

	@GetMapping("/showQuestionList")
	public String showQuestionList(Map<String, Object> model,
			@SessionAttribute("gradeHomework") GradeHomework gradeHomework) {

		String log_prepend = "[GET /showQuestionList]";
		String jspToDisplay = "graderView/list-hw-by-question";
		log.debug(log_prepend + " Displaying " + jspToDisplay);
		model.put("questionList", gradeHomeworkService.getListOfQuestions(gradeHomework.getHomework()));
		return jspToDisplay;
	}

	@GetMapping("/student")
	public String showQuestionListForStudent(@RequestParam("studentName") String studentName, Map<String, Object> model,
			@SessionAttribute("gradeHomework") GradeHomework gradeHomework) {

		String log_prepend = "[GET /student (" + studentName + ")]";
		String jspToDisplay = "graderView/list-question-for-student";

		log.debug(log_prepend + " Displaying " + jspToDisplay);
		String homework = gradeHomework.getHomework();
		model.put("questionListForStudent", gradeHomeworkService.getListOfQuestionsForStudent(homework, studentName));
		model.put("studentName", studentName);
		return jspToDisplay;

	}

	@GetMapping("/question")
	public String showStudentListForQuestion(@RequestParam("questionName") String questionName,
			Map<String, Object> model, @SessionAttribute("gradeHomework") GradeHomework gradeHomework) {

		String log_prepend = "[GET /question (" + questionName + ")]";
		String jspToDisplay = "graderView/list-student-for-question";

		log.debug(log_prepend + " Displaying " + jspToDisplay);
		String homework = gradeHomework.getHomework();
		model.put("studentListForQuestion", gradeHomeworkService.getListOfStudentsForQuestion(homework, questionName));
		model.put("questionName", questionName);
		return jspToDisplay;
	}

	@GetMapping("/questionStudent")
	public String showGradingView(@RequestParam("questionName") String questionName,
			@RequestParam("studentName") String studentName, Map<String, Object> model,
			@SessionAttribute("gradeHomework") GradeHomework gradeHomework) {

		String log_prepend = "[GET /questionStudent (" + questionName + ", " + studentName + ")]";
		String homework = gradeHomework.getHomework();
		String submissionLoc = submissionDBService.getSubmission(homework, studentName, questionName);
		log.debug(log_prepend +" Submission Location: "+ submissionLoc);
		
		// TODO get contents to display the view 
		
		return "TEMP";
	}
}