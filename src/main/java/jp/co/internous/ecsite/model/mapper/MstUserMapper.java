package jp.co.internous.ecsite.model.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import jp.co.internous.ecsite.model.domain.MstUser;
import jp.co.internous.ecsite.model.form.LoginForm;

@Mapper
public interface MstUserMapper {
	
	// 入力したユーザー名とパスワードに一致するものを探して返す
	@Select(value = "SELECT * FROM mst_user WHERE user_name = #{userName} AND password = #{password}")
	MstUser findByUserNameAndPassword(LoginForm form);
	
	// 入力した情報をDBに追加して、変更されたDBの行の数を返す
	@Insert("INSERT INTO mst_user (user_name, password, full_name) VALUES (#{userName}, #{password}, #{fullName})")
	@Options(useGeneratedKeys=true, keyProperty="id")
	int insert(MstUser user);
	
	@Select(value = "SELECT COUNT(*) FROM mst_user WHERE user_name = #{userName}")
	int checkIfUserExists(String userName);

}
