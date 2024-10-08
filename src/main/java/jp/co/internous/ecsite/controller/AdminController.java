package jp.co.internous.ecsite.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jp.co.internous.ecsite.model.domain.MstGoods;
import jp.co.internous.ecsite.model.domain.MstUser;
import jp.co.internous.ecsite.model.form.GoodsForm;
import jp.co.internous.ecsite.model.form.LoginForm;
import jp.co.internous.ecsite.model.mapper.MstGoodsMapper;
import jp.co.internous.ecsite.model.mapper.MstUserMapper;

@Controller
@RequestMapping("/ecsite/admin")
public class AdminController {
	
	@Autowired
	private MstUserMapper userMapper;
	
	@Autowired
	private MstGoodsMapper goodsMapper;
	
	@RequestMapping("/")
	public String index() {
		return "admintop";     // topページ(admintop.html)に遷移
	}
	
	
	// ブラウザでpost送信された場合のみ（URL直接入力によるget通信を省く）呼び出したい場合は、
	// PostMappingを使用する↓
	
	@PostMapping("/welcome")
	public String welcome(LoginForm form, Model model) {  // ブラウザの入力項目がLoginFormクラスの各フィールドに格納される
		
		// MstUserMapperのメソッドが呼ばれ、そこのSQL文が実行される
		// 実行結果がreturnされ、変数userに代入される
		MstUser user = userMapper.findByUserNameAndPassword(form);
		
		
		// ユーザーがヒットしなかった場合、fowardでtopページに遷移
		if (user == null) {
			model.addAttribute("errMessage", "ユーザー名またはパスワードが違います。");
			return "forward:/ecsite/admin/";
		}
		
		// ユーザーが管理者でない場合、fowardでtopページに遷移
		if (user.getIsAdmin() == 0) {
			model.addAttribute("errMessage", "管理者ではありません。");
			return "forward:/ecsite/admin/";
		}
		
		// 管理者としてログインに成功した場合
		// MstGoodsMapperのfindAllメソッドが呼ばれ、そこのSQL文を実行
		// 商品情報を検索し、HTMLに渡す情報をmodelに登録
		List<MstGoods> goods = goodsMapper.findAll();
		model.addAttribute("userName", user.getUserName());
		model.addAttribute("password", user.getPassword());
		model.addAttribute("goods", goods);
		
		return "welcome";   // ログイン後のページ(welcome.html)へ遷移
	}
	
	// 新規商品の登録ページへ遷移
	@PostMapping("/goodsMst")
	public String goodsMst(LoginForm f, Model m) {
		m.addAttribute("userName", f.getUserName());
		m.addAttribute("password", f.getPassword());
		
		return "goodsmst";  // ページ(goodsmst.html)へ遷移
	}
	
	// 新規商品情報をデータベースに登録するメソッド
	@PostMapping("/addGoods")
	public String addGoods(GoodsForm goodsForm, LoginForm loginForm, Model m) {
		m.addAttribute("userName", loginForm.getUserName());
		m.addAttribute("password", loginForm.getPassword());
		
		MstGoods goods = new MstGoods();
		goods.setGoodsName(goodsForm.getGoodsName());
		goods.setPrice(goodsForm.getPrice());
		
		goodsMapper.insert(goods);
		
		return "forward:/ecsite/admin/welcome";
	}
	
	// 商品を削除する機能
	@ResponseBody
	@PostMapping("/api/deleteGoods")
	public String deleteApi(@RequestBody GoodsForm f, Model m) {
		try {
			goodsMapper.deleteById(f.getId());
		} catch (IllegalArgumentException e) {
			return "-1";
		}
		
		return "1";
	}

}
