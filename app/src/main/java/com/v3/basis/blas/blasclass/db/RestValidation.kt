package com.v3.basis.blas.blasclass.db

class RestValidation {

    /*
	 * [機能]
	 * 英数字かどうかをチェックする。
	 * cakeのalphaNumericは日本語も抜けてしまうので
	 * DIYした。
	 * [戻り値]
	 * 英数字:true,日本語を含む場合false
	 */
    fun alphaNumeric(check: String) {
//        $value = array_values([$check]);  // 配列の添字を数値添字に変換
//        $value = $value[0];     // 最初の値を取る
//        return preg_match('/^[a-zA-Z0-9]+$/', $value);
    }

    /*
	 * [機能]
	 * 日付形式[yyyy-mm-dd hh:ii:ss]が正しく指定されているか判定する
	 * [戻り値]
	 * true:正しい
	 * false:フォーマット不正
	 */
    fun checkDatetimeFormat(datetime: String) {
//        return $datetime === date("Y-m-d H:i:s", strtotime($datetime));
    }


    /*
	 * [機能]
	 * 数字に関するパラメーターをチェックする
	 * [引数]
	 * $params POST,PUTまたはGETで取得したときのパラメーター
	 * $keys チェックしたいキーのリスト
	 * $required 必須の場合true、省略可能パラメーターの場合falseを指定する
	 * $range 値に範囲を指定する場合、[min, max]で指定する
	 *
	 */
    fun number_check(
        vararg params: String,
        required: Boolean = true,
        range: Pair<Int, Int>? = null
    ) {
//        if($required == true) {
//            foreach($keys as $key) {
//            if(!isset($params[$key])){
//            throw new UnexpectedValueException(sprintf($this->RestMessage->MESSAGES[300], $key), 300);
//        }
//            if(!Validation::numeric($params[$key])) {
//            /* 数字かどうかチェック */
//            throw new UnexpectedValueException(sprintf($this->RestMessage->MESSAGES[303], $key, $params[$key]), 303);
//        }
//            if($range != null) {
//            $min = $range[0];
//            $max = $range[1];
//            if($params[$key] < $min || $params[$key] > $max) {
//            throw new UnexpectedValueException(sprintf($this->RestMessage->MESSAGES[305], $min, $max, $params[$key]), 305);
//        }
//        }
//        }
//        }
//        else {
//            foreach($keys as $key) {
//            if(isset($params[$key])) {
//            if (!Validation::numeric($params[$key])) {
//            /* 数字かどうかチェック */
//            throw new UnexpectedValueException(sprintf($this->RestMessage->MESSAGES[303], $key, $params[$key]), 303);
//        }
//            if ($range != null) {
//            $min = $range[0];
//            $max = $range[1];
//            if ($params[$key] < $min || $params[$key] > $max) {
//            throw new UnexpectedValueException(sprintf($this->RestMessage->MESSAGES[305], $min, $max, $params[$key]), 305);
//        }
//        }
//        }
//        }
//        }
    }

    /*
	 * [機能]
	 * 英数字に関するパラメーターをチェックする
	 * [引数]
	 * $params POST,PUTまたはGETで取得したときのパラメーター
	 * $keys チェックしたいキーのリスト
	 * $required 必須の場合true、省略可能パラメーターの場合falseを指定する
	 * [例外]
	 * 不正なパラメーターの場合はUnexpectedValueExceptionをスローする
	 */
    fun alphaNumeric_check(vararg params: String, required: Boolean = true) {
//        if($required == true) {
//            foreach($keys as $key) {
//            if(!isset($params[$key])){
//            throw new UnexpectedValueException(sprintf($this->RestMessage->MESSAGES[300], $key), 300);
//        }
//            if(!$this->alphaNumeric($params[$key])) {
//            /* 数字かどうかチェック */
//            throw new UnexpectedValueException(sprintf($this->RestMessage->MESSAGES[303], $key, $params[$key]), 303);
//        }
//        }
//        }
//        else {
//            foreach($keys as $key) {
//            if(isset($params[$key])) {
//            if (!!$this->alphaNumeric($params[$key])) {
//            /* 数字かどうかチェック */
//            throw new UnexpectedValueException(sprintf($this->RestMessage->MESSAGES[303], $key, $params[$key]), 303);
//        }
//        }
//        }
//        }
    }

    /*
	 * [機能]
	 * 日付時刻に関するパラメーターをチェックする
	 * [引数]
	 * $params POST,PUTまたはGETで取得したときのパラメーター
	 * $keys チェックしたいキーのリスト
	 * $required 必須の場合true、省略可能パラメーターの場合falseを指定する
	 *
	 */
    fun datetime_check(vararg params: String, required: Boolean = true) {
//        if($required == true)
//        {
//            foreach($keys as $key) {
//            if (!isset($params[$key])){
//            throw new UnexpectedValueException (sprintf($this->RestMessage->MESSAGES[300], $key), 300);
//        }
//            if (!$this->checkDatetimeFormat($params[$key])) {
//            /* 日付フォーマットチェック */
//            throw new UnexpectedValueException (sprintf($this->RestMessage->MESSAGES[303], $key, $params[$key]), 303);
//        }
//        }
//        }
//        else
//        {
//            foreach($keys as $key) {
//            if (isset($params[$key])) {
//            if (!$this->checkDatetimeFormat($params[$key])) {
//            /* 日付フォーマットチェック */
//            throw new UnexpectedValueException (sprintf($this->RestMessage->MESSAGES[303], $key, $params[$key]), 303);
//        }
//        }
//        }
//        }
    }

    /*
	 * [機能]
	 * テキストに関するパラメーターをチェックする
	 * [引数]
	 * $params POST,PUTまたはGETで取得したときのパラメーター
	 * $keys チェックしたいキーのリスト
	 * $required 必須の場合true、省略可能パラメーターの場合falseを指定する
	 *　$blank 空文字""を許可する
	 */
    fun text_check(vararg params: String, required: Boolean = true, blank: Boolean = true) {
//            if ($required == true) {
//            foreach($keys as $key) {
//            if (!isset($params[$key])){
//            throw new UnexpectedValueException (sprintf($this->RestMessage->MESSAGES[300], $key), 300);
//        }
//            if (!$blank) {
//                if (strlen($params[$key]) == 0) {
//                    /* テキストの文字の長さチェック */
//                    throw new UnexpectedValueException (sprintf($this->RestMessage->MESSAGES[303], $key, $params[$key]), 303);
//                }
//            }
//        }
//        }
//            else {
//            foreach($keys as $key) {
//            if (isset($params[$key])) {
//            if (!$blank) {
//                if (count($params[$key]) == 0) {
//                    /* 日付フォーマットチェック */
//                    throw new UnexpectedValueException (sprintf($this->RestMessage->MESSAGES[303], $key, $params[$key]), 303);
//                }
//            }
//        }
//        }
//        }
    }
}
