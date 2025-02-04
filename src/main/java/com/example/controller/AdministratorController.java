package com.example.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.domain.Administrator;
import com.example.form.InsertAdministratorForm;
import com.example.form.LoginForm;
import com.example.service.AdministratorService;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;

import jakarta.servlet.http.HttpSession;

/**
 * 管理者情報を操作するコントローラー.
 * 
 * @author igamasayuki
 *
 */
@Controller
@RequestMapping("/")
public class AdministratorController {

	

	@Autowired
	private AdministratorService administratorService;

	@Autowired
	private HttpSession session;

	/**
	 * 使用するフォームオブジェクトをリクエストスコープに格納する.
	 * 
	 * @return フォーム
	 */
	@ModelAttribute
	public InsertAdministratorForm setUpInsertAdministratorForm() {
		return new InsertAdministratorForm();
	}

	/**
	 * 使用するフォームオブジェクトをリクエストスコープに格納する.
	 * 
	 * @return フォーム
	 */
	@ModelAttribute
	public LoginForm setUpLoginForm() {
		return new LoginForm();
	}

	/////////////////////////////////////////////////////
	// ユースケース：管理者を登録する
	/////////////////////////////////////////////////////
	/**
	 * 管理者登録画面を出力します.
	 * 
	 * @return 管理者登録画面
	 */
	@GetMapping("/toInsert")
	public String toInsert() {
		return "administrator/insert";
	}

	/**
	 * 管理者情報を登録します.
	 * 
	 * @param form 管理者情報用フォーム
	 * @return ログイン画面へリダイレクト
	 */
	@PostMapping("/insert")
	public String insert(@Validated InsertAdministratorForm form, BindingResult result, RedirectAttributes redirectAttributes) {
		try {
			Administrator administrator = new Administrator();
			// フォームからドメインにプロパティ値をコピー
			BeanUtils.copyProperties(form, administrator);
			administratorService.insert(administrator);
		} catch (IllegalArgumentException e) {
			// メールアドレス重複のエラーをキャッチしてリダイレクト時にエラーメッセージを渡す
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			return "redirect:/administrator/toInsert"; // エラー時に登録画面へリダイレクト
		}
		return "redirect:/employee/showList"; // 登録成功時に従業員一覧へリダイレクト
	}

	/////////////////////////////////////////////////////
	// ユースケース：ログインをする
	/////////////////////////////////////////////////////
	/**
	 * ログイン画面を出力します.
	 * 
	 * @return ログイン画面
	 */
	@GetMapping("/")
	public String toLogin() {
		return "administrator/login";
	}

	/**
	 * ログインします.
	 * 
	 * @param form 管理者情報用フォーム
	 * @return ログイン後の従業員一覧画面
	 * 
	 * 
	 * */
	 @PostMapping("/login")
	 public String login(LoginForm form, RedirectAttributes redirectAttributes, Model model) {
        // 管理者の認証を行う
        Administrator administrator = administratorService.login(form.getMailAddress(), form.getPassword());
        
        if (administrator == null) {
            // ログイン失敗時にエラーメッセージをリダイレクト属性に追加
            redirectAttributes.addFlashAttribute("errorMessage", "メールアドレスまたはパスワードが不正です。");
            return "redirect:/"; // ログイン失敗時にリダイレクト
        }

        // ログイン成功時にセッションに管理者名を保存
        session.setAttribute("administratorName", administrator.getName());
		System.out.println("セッションに保存された名前: " + session.getAttribute("administratorName"));
		
		model.addAttribute("administratorName", administrator.getName());

        
        return "redirect:/employee/showList"; // ログイン後に従業員一覧画面へリダイレクト
    }

	/////////////////////////////////////////////////////
	// ユースケース：ログアウトをする
	/////////////////////////////////////////////////////
	/**
	 * ログアウトをします. (SpringSecurityに任せるためコメントアウトしました)
	 * 
	 * @return ログイン画面
	 */
	@GetMapping(value = "/logout")
	public String logout() {
		session.invalidate();
		return "redirect:/";
	}

}
