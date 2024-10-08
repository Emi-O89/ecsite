package jp.co.internous.ecsite.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import jp.co.internous.ecsite.model.domain.MstGoods;
import jp.co.internous.ecsite.model.domain.MstUser;
import jp.co.internous.ecsite.model.dto.HistoryDto;
import jp.co.internous.ecsite.model.form.CartForm;
import jp.co.internous.ecsite.model.form.HistoryForm;
import jp.co.internous.ecsite.model.form.LoginForm;
import jp.co.internous.ecsite.model.form.SignupForm;
import jp.co.internous.ecsite.model.mapper.MstGoodsMapper;
import jp.co.internous.ecsite.model.mapper.MstUserMapper;
import jp.co.internous.ecsite.model.mapper.TblPurchaseMapper;

@Controller
@RequestMapping("/ecsite")
public class IndexController {
	
	@Autowired
	private MstGoodsMapper goodsMapper;
	
	@Autowired
	private MstUserMapper userMapper;
	
	@Autowired
	private TblPurchaseMapper purchaseMapper;
	
	private Gson gson = new Gson();
	
	// トップページに遷移
	// goodsテーブルから取得した商品エンティティの一覧を、フロントに渡すModelに追加する
	@GetMapping("/")
	public String index(Model model) {
		List<MstGoods> goods = goodsMapper.findAll();
		model.addAttribute("goods", goods);
		
		return "index";   // index.htmlに遷移
	}
	
	// login関数のfetchからこちらへ
	@ResponseBody  // ←文字そのものが返却される
	@PostMapping("/api/login")
	public String loginApi(@RequestBody LoginForm f) {
		
		// mst_userテーブルからユーザー名とPWで検索し、結果を取得
		MstUser user = userMapper.findByUserNameAndPassword(f);
		
		if (user == null) {
			user = new MstUser();
			user.setFullName("ゲスト");
		}
		
		return gson.toJson(user);  // MstUser型のuserをJSON形式の文字列に変換
	}
	
	// purchase関数からこちらへ
	@ResponseBody
	@PostMapping("/api/purchase")
	public int purchaseApi(@RequestBody CartForm f) {
		
		f.getCartList().forEach((c) -> {
			int total = c.getPrice() * c.getCount();
			purchaseMapper.insert(f.getUserId(), c.getId(), c.getGoodsName(), c.getCount(), total);
		});
		
		return f.getCartList().size();
	}
	
	@ResponseBody
	@PostMapping("/api/history")
	public String historyApi(@RequestBody HistoryForm f) {
		int userId = f.getUserId();
		List<HistoryDto> history = purchaseMapper.findHistory(userId);
		
		return gson.toJson(history);
	}
	
	// ユーザー 新規登録の画面（signup.html）を表示する
	@GetMapping("/signup")
	public String signup() {
		return "signup";
	}
	
	// ユーザー新規登録
	@PostMapping("/signup")
	public String signupRedirect(SignupForm f, Model m) {
		
		// 入力されたフォームの情報をmに保存する（表示したままにするため）
		m.addAttribute("fullName", f.getFullName());
		m.addAttribute("userName", f.getUserName());
		m.addAttribute("password", f.getPassword());
		
		// もし入力されたものが空欄なら
		if (f.getFullName() == null || f.getFullName().isEmpty() || 
			f.getUserName() == null || f.getUserName().isEmpty() ||
			f.getPassword() == null || f.getPassword().isEmpty()) {
			
			//↑ 上のコードは下記でもOK
			// f.getFullName() == "" || f.getUserName() == "" || f.getPassword() == ""
			
			// エラーメッセージ
			m.addAttribute("errMessage", "全ての項目を入力してください。");
			
			return "signup";
		}
		
		// 入力したユーザー名がすでにDBにあるかどうか確認
		Integer userExist = userMapper.checkIfUserExists(f.getUserName());
		
		if (userExist > 0) {
			
			// エラーメッセージ
			m.addAttribute("errMessage", "このユーザー名はすでに使用されています。");			
			
			return "signup";
		}
		
		// エラーがなかった場合（正常に登録）
		MstUser user = new MstUser();
		user.setFullName(f.getFullName());
		user.setUserName(f.getUserName());
		user.setPassword(f.getPassword());
        user.setIsAdmin(0);
		
		userMapper.insert(user);
		
		return "redirect:/ecsite/";
	}

}
