package com.v3.basis.blas.blasclass.extra.trivia

import android.util.Log

class TriviaList {
    val triviaList = mutableListOf<MutableMap<String,String>>()
    val trivias = mutableMapOf<String,String>()
    init {
        //偉人シリーズ
        trivias["べートーベンは大の引っ越し好きだった？"] = "ベートーベンは、引っ越しが大好きだった。"
        trivias["アインシュタインは読み書きができなかった？"]= "アインシュタインは、4歳まで言葉が話せず、7歳まで文字が読めなかった。"
        trivias["ケネディはいたずら好きだった？"]= "ケネディは、学生時代トイレに爆竹を投げて遊んでいたらしい。"
        trivias["一休さんの死因とは？"] = "一休さんこと、一休宗純の死因はマラリアだったらしい。"
        trivias["遠山の金さんの弱点とは？"] = "遠山の金さんは、実は痔を患っていたらしい"
        trivias["豊臣秀吉が隠していたものとは？"] = "豊臣秀吉は、付け髭をしていたらしい。"
        trivias["武田信玄の苦手なものとは？"] = "武田信玄は、芋虫が大の苦手だったらしい。"
        trivias["ヒトラーがなりたかったものとは？"] = "ヒトラーは画家になるのが夢だった。しかし、美大に落ちてあきらめた。"
        trivias["ソ連の独裁者スターリンの過去とは？"] = "子供のころ、スターリンはかなり病弱だった。加えて、幼少期に患った天然痘の後遺症が大人になっても残っていたらしい"
        trivias["ビルゲイツがしようとした犯罪とは？"] = "ビルゲイツは、ある会社にハッキングを試したらしい。ただ、失敗したそうな。"

        //動物とか雑学とかシリーズ
        trivias["ヤギって実は紙を食べると困る？"] = "ヤギは紙を見るとついつい食べるそうな。ただ、消化できずにおなかを壊す。"
        trivias["パンダの食べている笹。実は,,,"] = "栄養が全くないそうです。"
        trivias["日本で一番多い、フルネームとは？"] = "日本人で一番多いフルネームは「田中実」(2018年時)。約5300人いるそうです。"
        trivias["おにぎりとおむすびの違いとは？"] = "おにぎりは形関係なくご飯を握ったもの。おむすびは三角形にご飯を握ったもの。"
        trivias["運転中に聞いてはいけない音楽とは？"] = "運転中にワーグナー作曲の「ワルキューレの騎行」を聞いていると事故を起こしやすくなる。"
        trivias["ゴリ押しのゴリってなに？"] = "ゴリ押しのゴリとは、魚のゴリが由来らしいよ"
       //これ変更 trivias["トイレの夢を見ると..."] = "トイレの夢は幸運(ウン)を呼び込む吉夢らしいよ(真面目に)。"
        trivias["蛇の夢を見ると..."] = "蛇の夢は金運アップの吉夢らしいよ。特に白蛇が一番いいみたい。"
        trivias["分身の術の真実"] = "忍者が使う分身の術は存在していた。方法の一つとして、暗示にかける手法があるらしい。"
        trivias["江戸時代の忍者は何していたの？"] = "争いの少ない江戸時代では、忍者は火遁の術を使った大道芸などをして生活していたらしい。"

        trivias.forEach{ (key, value) ->
            val trivia:MutableMap<String,String> = mutableMapOf(key to value)
            triviaList.add(trivia)
        }
    }

    fun size() : Int {
        return triviaList.size
    }
    fun getTrivia(index:Int): MutableMap<String, String> {
        return triviaList[index]
    }

    fun getTriviaType(): Int {
        return triviaList.size
    }
}