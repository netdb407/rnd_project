import com.mongodb.spark._
import com.mongodb.spark.config._
import org.apache.spark._
import org.apache.spark.rdd.RDD
import com.mongodb.spark.rdd.MongoRDD
import com.mongodb.spark.sql._
import org.bson.Document
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame
import java.sql.Timestamp
import java.time.Duration

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.IntegerType
import org.apache.spark.sql.Column
import org.apache.log4j.Logger
import org.apache.log4j.Level

import scala.collection.mutable

val base ="mongodb://127.0.0.1/cpmongo."

val base ="mongodb://127.0.0.1/cpmongo_distinct."
val output_base = "mongodb://127.0.0.1/cpmongo_distinct.USER_SIMILARITY"
val SREG_output_base = "mongodb://127.0.0.1/cpmongo_distinct.SREG_SIM"
val NCR_output_base = "mongodb://127.0.0.1/cpmongo_distinct.NCR_SIM"
val ACT_output_base = "mongodb://127.0.0.1/cpmongo_distinct.ACTIVITY_SIM"
//교과: SREG_SIM, 비교과: NCR_SIM, 자율활동: ACTIVITY_SIM
val Result_output_base = "mongodb://127.0.0.1/cpmongo_distinct.REC_RESULT"


val replyUri = "CPS_BOARD_REPLY"  //댓글
val codeUri = "CPS_CODE_MNG"  //통합 코드관리 테이블
val gradCorpUri = "CPS_GRADUATE_CORP_INFO"  //졸업 기업
val ncrInfoUri = "CPS_NCR_PROGRAM_INFO"  //비교과 정보
val ncrStdInfoUri = "CPS_NCR_PROGRAM_STD"  //비교과 신청학생
val outActUri = "CPS_OUT_ACTIVITY_MNG"  //교외활동
val jobInfoUri = "CPS_SCHOOL_EMPLOY_INFO"  //채용정보-관리자 등록
val sjobInfoUri = "CPS_SCHOOL_EMPLOY_STD_INFO"  //채용정보 신청 학생 정보(student job info)


val deptInfoUri = "V_STD_CDP_DEPT"  //학과 정보 (department info)
val clPassUri = "V_STD_CDP_PASSCURI" //교과목 수료(class pass)
val stInfoUri = "V_STD_CDP_SREG"  //학생 정보 (student info)
val pfInfoUri = "V_STD_CDP_STAF"  //교수 정보 (professor info)
val clInfoUri = "V_STD_CDP_SUBJECT"  //교과 정보 (class info)

val cpsStarUri = "CPS_STAR_POINT"  //교과/비교과용 별점 테이블
val userSimilarityUri = "USER_SIMILARITY" //유사도 분석 팀이 생성한 테이블


val replyUri_table = getMongoDF(spark, replyUri)  //댓글
val codeUri_table =  getMongoDF(spark, codeUri)  //통합 코드관리 테이블
val gradCorpUri_table =  getMongoDF(spark, gradCorpUri)  //졸업 기업
val ncrInfoUri_table =  getMongoDF(spark, ncrInfoUri)  //비교과 정보
val ncrStdInfoUri_table =  getMongoDF(spark, ncrStdInfoUri)  //비교과 신청학생
val outActUri_table =  getMongoDF(spark, outActUri)  //교외활동
val jobInfoUri_table =  getMongoDF(spark, jobInfoUri)  //채용정보-관리자 등록
val sjobInfoUri_table =  getMongoDF(spark, sjobInfoUri)  //채용정보 신청 학생 정보(student job info)


val deptInfoUri_table =  getMongoDF(spark, deptInfoUri)  //학과 정보 (department info)
val clPassUri_table =  getMongoDF(spark, clPassUri) //교과목 수료(class pass)
val stInfoUri_table =  getMongoDF(spark, stInfoUri)  //학생 정보 (student info)
val pfInfoUri_table =  getMongoDF(spark, pfInfoUri)  //교수 정보 (professor info)
val clInfoUri_table =  getMongoDF(spark, clInfoUri)  //교과 정보 (class info)

val cpsStarUri_table = getMongoDF(spark, cpsStarUri)  //교과/비교과용 별점 테이블
val userSimilarity_table = getMongoDF(spark, userSimilarityUri) //유사도 분석 팀이 생성한 테이블

Logger.getLogger("org").setLevel(Level.OFF)
Logger.getLogger("akka").setLevel(Level.OFF)
Logger.getLogger("MongoRelation").setLevel(Level.OFF)
Logger.getLogger("MongoClientCache").setLevel(Level.OFF)

def getMongoDF(
                spark : SparkSession,
                coll : String ) : DataFrame = {
  spark.read.mongo(ReadConfig(Map("uri"->(base+coll))))
}

//새로 수정(연희 수정)
def setMongoDF(
                spark : SparkSession,
                df : DataFrame ) = {
  df.saveToMongoDB(WriteConfig(Map("uri"->(output_base))))
}

//setMongoDF(spark, dataframe명)
val base ="mongodb://127.0.0.1/cpmongo."
val base2 = "mongodb://127.0.0.1/cpmongo_distinct."

def getMongoDF(
                spark : SparkSession,
                coll : String ) : DataFrame = {
  spark.read.mongo(ReadConfig(Map("uri"->(base+coll))))
}

def getMongoDF2(
                 spark : SparkSession,
                 coll : String ) : DataFrame = {
  spark.read.mongo(ReadConfig(Map("uri"->(base2+coll))))
}

//저장하기 setMongo(spark, Uri, dataframe)으로 사용
def setMongoDF(
                spark : SparkSession,
                coll: String,
                df : DataFrame ) = {
  df.saveToMongoDB(WriteConfig(Map("uri"->(base+coll))))
}

//추천결과팀 데이터 저장
def setMongoDF_result(
                       spark : SparkSession,
                       df : DataFrame ) = {
  df.saveToMongoDB(WriteConfig(Map("uri"->(Result_output_base))))
}


val test = getMongoDF2(spark, gradCorpUri)

val rdd = test.select("GCI_CORP_NM") //로우로 해당 컬럼들 읽어옴
val rdd2 = test.select("*")

val corps = rdd.collect.distinct.map(_.toSeq).flatten //읽어온 로우들 한 Seq에 박는 거

def shit(corps: Array[Any]):scala.collection.mutable.Map[Any, Array[org.apache.spark.sql.Row]]={
  var a = scala.collection.mutable.Map[Any, Array[org.apache.spark.sql.Row]]()
  for(i<-0 until corps.size){
    a = a+(corps(i)->rdd2.filter(rdd2("GCI_CORP_NM")===corps(i)).collect)
  }
  a}

val corps1 = shit(corps)

//기업 가중치 읽어오는 거
val test2 = getMongoDF(spark, "CPS_RATING")
var tuples = Seq[(Int, Double)]()
val t5 = test.select(col("GCI_STD_NO"), col("GCI_CORP_NM")).distinct.toDF

// (사용자 신뢰도)
for(i<-0 until corps.size){
  val a = 0.2
  val b = 0.125
  val w1 = 0.5
  val w2 = 0.5
  val columns = Seq("GCI_STD_NO", "RATING")
  var df = test2.filter(test2("기업명")===corps(i)).toDF
  var filter = t5.filter(t5("GCI_CORP_NM").equalTo(corps(i))).collect
  if(df.collect.size>0){
    var add = df.select("기업가중치").as[String].collect()(0).toInt*w1*a+df.select("직원수 가중치").as[String].collect()(0).toInt*w2*b
    for(j<-0 until filter.size){
      //      tuples = tuples :+ (filter(j)(0).toString.toInt, add.toString.toDouble)
      tuples = tuples :+ (filter(j)(0).toString.toInt, add.toString.toDouble)
    }
  }else{
    for(j<-0 until filter.size){
      tuples = tuples :+ (filter(j)(0).toString.toInt, 0.0)
    }
  }
}

//콘텐츠 신뢰도
val test0 = getMongoDF(spark, "CPS_STAR_POINT")
var con = test0.select(col("STAR_KEY_ID"), col("STAR_POINT"))
var con1 = con.groupBy("STAR_KEY_ID").agg(avg("STAR_POINT").alias("STAR_POINT"))

//코드 다 실행하고 결과 출력해보는 거
val user_sim_df = user_sim_tuples.toDF("STD_NO", "SIMILARITY")
val user_trust_df = tuples.toDF("STD_NO", "TRUST")

user_sim_df.show()
user_trust_df.show()
con1.show()

//유사도+신뢰도 join

val user_Result_df = user_trust_df.join(user_sim_df, Seq("STD_NO"), "outer")
val user_Result_df_NaN = user_Result_df.na.fill(0.0, List("SIMILARITY")).na.fill(0.0, List("TRUST"))

//val newDf = user_Result_df_NaN.select(col("SIMILARITY").map(col("TRUST")).reduce((c1, c2) => c1 + c2) as "sum")

//유사도+신뢰도 join DF->list변환

val a = double2Double(1)
val b = double2Double(1)
val w1 = 0.5
val w2 = 0.5

val res_ex_str = "\\[|\\]"
val user_Result1 = user_Result_df_NaN.select("STD_NO", "SIMILARITY", "TRUST").collect.map(_.toString.replaceAll(res_ex_str, "")).map{ row =>
  val x = row.split(",")
  val stdNo = x(0)
  val sim = x(1).toDouble * a * w1
  val tru = x(2).toDouble * b * w2
  (stdNo, sim, tru, sim + tru)
}.sortBy(x=> x._4).reverse

def roundAt(p: Int)(n: Double): Double = { val s = math pow (10, p); (math round n * s) / s }

//유사도 + 신뢰도결합 최종 추천학생 list
val std_arr = user_Result1.map(x=>x._1).toSeq.toList.take(10)

// 교과목수료 테이블 중 학번, 학과, 교과목번호, 과목명

val clPassUri_table =  getMongoDF(spark, clPassUri) //교과목 수료(class pass)

//유사도팀이랑 합치면 없애야되는부분 => 중복제거하면 값 1개만불러와짐
var clPassUri_DF = clPassUri_table.select(col("STD_NO"), col("SUST_CD_NM"), col("SBJT_KOR_NM"), col("SBJT_KEY_CD")).distinct.toDF

//한학생이 들은 수업리스트(질의자)
var std_NO = 20152611
var sbjtNM_by_stdNO = clPassUri_DF.filter(clPassUri_DF("STD_NO").equalTo(s"${std_NO}")).select(col("SBJT_KEY_CD")).distinct
var sbjtNM_by_stdNO_List = sbjtNM_by_stdNO.rdd.map(r=>r(0)).collect.toList.distinct.map(_.toString)
///////////////////////////////////////

//val std_arr = Seq(20190030, 20170063, 20142915, 20152634, 20142824, 20161627)

val std_list = std_arr.map{ stdno =>
  val res = clPassUri_DF.filter(clPassUri_DF("STD_NO").equalTo(s"${stdno}"))
  res
}.map{ x =>
  val res = x.select("SBJT_KEY_CD").collect.map( x=> x.toString)
  res
}.flatMap( x=> x)

//질의학생이 들은 과목 중복제거
val exStr = "\\[|\\]"
val t2 = std_list.map(x => x.replaceAll(exStr, ""))
val std_filter = t2.diff(sbjtNM_by_stdNO_List)

val std_sbjt_arr = std_filter.groupBy(x => x).mapValues(_.length).toList.sortBy(x => x._2).reverse

//콘텐츠 신뢰도 DF->list변환(reduceDF_con1)
import org.apache.spark.sql.functions.collect_list
var reducedDF_con1 = con1.select("STAR_KEY_ID", "STAR_POINT").distinct()

//콘텐츠 신뢰도 형변환
val reducedDF_con2 = reducedDF_con1.groupBy("STAR_KEY_ID").agg(collect_list($"STAR_POINT").as("STAR_POINT")).rdd.map(row => (row(0).toString -> row(1).toString)).collect.map{ x =>
  val sbjt = x._1
  val starPoint = x._2.slice(13,x._2.length-1).toDouble
  (sbjt, starPoint)
}

//콘텐츠신뢰도 STAR_KEY_ID와 교과목수료 SBJT_KEY_CD일치 여부에 따라 값 곱하기
val res_arr = std_sbjt_arr.map { x =>
  val sbjt = x._1
  val filtered_sbjt_arr = reducedDF_con2.filter( x=> x._1 == sbjt)
  val v = if(filtered_sbjt_arr.isEmpty) x._2
  else filtered_sbjt_arr(0)._2 * x._2
  (sbjt, v)
}

//교과목 결과값에 콘텐츠 신뢰도를 곱해 재 랭킹
val PassUri_top5_list = res_arr.sortBy(x => x._2).reverse


//비교과 신청학생 테이블 중 학번, 비교과 프로그램 학생키아이디, 비교과 프로그램 학생키아이디, 과목명
//NPS_STATE 코드 (NCR_T07_P00 : 대기신청, NCR_T07_P01 : 승인대기, NCR_T07_P02 :승인, NCR_T07_P03 : 학생취소,
//NCR_T07_P04 : 관리자취소, NCR_T07_P05 : 이수, NCR_T07_P06 : 미이수, NCR_T07_P07 : 반려)
val ncrStdInfoUri_table =  getMongoDF(spark, ncrStdInfoUri)

var ncrInfoUri_DF = ncrStdInfoUri_table.select(col("NPS_STD_NO"), col("NPS_KEY_ID"), col("NPI_KEY_ID"), col("NPS_STATE")).distinct.toDF
ncrInfoUri_DF.show()

//한학생이 들은 비교과리스트
var std_NO = 20152611
var ncrStdInfoUri_stdNO = ncrInfoUri_DF.filter(ncrInfoUri_DF("NPS_STD_NO").equalTo(s"${std_NO}")).select(col("NPI_KEY_ID")).distinct
var ncrStdInfoUri_stdNO_List = ncrStdInfoUri_stdNO.rdd.map(r=>r(0)).collect.toList.distinct.map(_.toString)
////////////////////////////////////////////////////////////////////////////////////////////

val ncrStdInfoUri_arr = std_arr.map{ stdno =>
  val res = ncrInfoUri_DF.filter(ncrInfoUri_DF("NPS_STD_NO").equalTo(s"${stdno}")).filter($"NPS_STATE" === "NCR_T07_P05")
  res
}.map{ x =>
  val res = x.select("NPI_KEY_ID").collect.map( x=> x.toString)
  res
}.flatMap( x=> x)

//질의학생이 들은 과목 중복제거
val exStr = "\\[|\\]"
val ncr_replace = ncrStdInfoUri_arr.map(x => x.replaceAll(exStr, ""))
val ncr_filter = ncr_replace.diff(ncrStdInfoUri_stdNO_List)

val ncr_filter_arr = ncr_filter.groupBy(x => x).mapValues(_.length).toList.sortBy(x => x._2).reverse

//콘텐츠신뢰도 STAR_KEY_ID와 비교과 NPI_KEY_ID 일치 여부에 따라 값 곱하기
val res_arr2 = ncr_filter_arr.map { x =>
  val ncr = x._1
  val filtered_ncrStdInfoUri = reducedDF_con2.filter( x=> x._1 == ncr)
  val v = if(filtered_ncrStdInfoUri.isEmpty) x._2
  else filtered_ncrStdInfoUri(0)._2 * x._2
  (ncr, v)
}


//-------------------- # # # 자율활동 리스트 # # # ------------------------------
//from.교외활동 CPS_OUT_ACTIVITY_MNG : 학번(OAM_STD_NO), 활동구분코드(OAM_TYPE_CD), 활동명(OAM_TITLE)
//자격증(CD01) : 이름(OAM_TITLE) / ex. 토익800~900, FLEX 일본어 2A,  FLEX 일본어 1A,  FLEX 중국어 1A
//어학(CD02) : 이름(OAM_TITLE)
//봉사(CD03), 대외활동(CD04), 기관현장실습(CD05) : 활동구분코드(OAM_TYPE_CD)

val outActUri_table =  getMongoDF(spark, outActUri)  //교외활동
val clPassUri_table =  getMongoDF(spark, clPassUri) //교과목 수료(class pass)
// 자율활동 테이블 중 학번, 학과, 교과목번호, 과목명
var outActUri_DF = outActUri_table.select(col("OAM_STD_NO"), col("OAM_TYPE_CD"), col("OAM_TITLE"))
outActUri_DF.show()

//유사도팀
var outAct_name_temp1 = outActUri_DF.filter(outActUri_DF("OAM_STD_NO").equalTo(s"${std_NO}")).select(col("OAM_STD_NO"), col("OAM_TITLE"), col("OAM_TYPE_CD")).distinct
//3개의 코드만 필터링
var outAct_name_temp2 = outAct_name_temp1.drop("OAM_STD_NO", "OAM_TYPE_CD").filter($"OAM_TYPE_CD" === "OAMTYPCD01" || $"OAM_TYPE_CD" ==="OAMTYPCD02").distinct
var outAct_name_List = outAct_name_temp2.rdd.map(r=>r(0)).collect.toList

//---------------------자율활동 추천 code list(자격증 CD01, 어학 CD02)----------------------


val outActUri_CD01_arr = std_arr.map{ stdno =>
  val res = outActUri_DF.filter(outActUri_DF("OAM_STD_NO").equalTo(s"${stdno}"))
  res
}.map{ x =>
  val res = x.select("OAM_TITLE").filter($"OAM_TYPE_CD" === "OAMTYPCD01").collect.toList.map( x=> x.toString)
  res
}.flatMap( x=> x).groupBy(x => x).mapValues(_.length).toList.sortBy(x => x._2).reverse


val outActUri_CD02_arr = std_arr.map{ stdno =>
  val res = outActUri_DF.filter(outActUri_DF("OAM_STD_NO").equalTo(s"${stdno}"))
  res
}.map{ x =>
  val res = x.select("OAM_TITLE").filter($"OAM_TYPE_CD" ==="OAMTYPCD02").collect.toList.map( x=> x.toString)
  res
}.flatMap( x=> x).groupBy(x => x).mapValues(_.length).toList.sortBy(x => x._2).reverse


//---------------------자율활동 추천 code list(봉사 CD03, 대외활동 CD04, 기관현장실습 CD05)----------------------

val outActUri_CD03 = std_arr.map{ stdno =>
  val res = outActUri_DF.filter(outActUri_DF("OAM_STD_NO").equalTo(s"${stdno}"))
  res
}.map{ x =>
  val res = x.select("OAM_TYPE_CD").filter($"OAM_TYPE_CD" === "OAMTYPCD03").collect.toList
  res
}.flatMap( x=> x).groupBy(x => x).mapValues(_.length).toList.sortBy(x => x._2).reverse

val outActUri_CD03_result = outActUri_CD03.map(x => x._2)

case class OAM_TYPE_CD(OAM_TYPE_CD: Int, count : Int)
val outActUri_CD03_arr = outActUri_CD03_result.map{ row =>
  val avg = row / std_arr.length
  val res3 = ("OAM_TYPE_CD03", avg)
  res3
}

val outActUri_CD04 = std_arr.map{ stdno =>
  val res = outActUri_DF.filter(outActUri_DF("OAM_STD_NO").equalTo(s"${stdno}"))
  res
}.map{ x =>
  val res = x.select("OAM_TYPE_CD").filter($"OAM_TYPE_CD" ==="OAMTYPCD04").collect.toList
  res
}.flatMap( x=> x).groupBy(x => x).mapValues(_.length).toList.sortBy(x => x._2).reverse

val outActUri_CD04_result = outActUri_CD04.map(x => x._2)

case class OAM_TYPE_CD(OAM_TYPE_CD: Int, count : Int)
val outActUri_CD04_arr = outActUri_CD04_result.map{ row =>
  val avg = row / std_arr.length
  val res3 = ("OAM_TYPE_CD04", avg)
  res3
}

val outActUri_CD05 = std_arr.map{ stdno =>
  val res = outActUri_DF.filter(outActUri_DF("OAM_STD_NO").equalTo(s"${stdno}"))
  res
}.map{ x =>
  val res = x.select("OAM_TYPE_CD").filter($"OAM_TYPE_CD" ==="OAMTYPCD05").collect.toList
  res
}.flatMap( x=> x).groupBy(x => x).mapValues(_.length).toList.sortBy(x => x._2).reverse

val outActUri_CD05_result = outActUri_CD05.map(x => x._2)

case class OAM_TYPE_CD(OAM_TYPE_CD: Int, count : Int)
val outActUri_CD05_arr = outActUri_CD05_result.map{ row =>
  val avg = row / std_arr.length
  val res3 = ("OAM_TYPE_CD05", avg)
  res3
}

////---------------------------------교과(sbjt_df), 비교과(ncr_df), 자율활동(act_df) join (Key값 필요) -------------------------------

val maxSize = 5
var colSet = scala.collection.mutable.Set[String]()

val PassUri_top5 = res_arr.sortBy(x => x._2).reverse.take(maxSize).zipWithIndex.map(x => (x._2 + 1, x._1._1)).toDF("Rank","SBJT_KEY_CD")
val NCR_std_Info_top5 = res_arr2.sortBy(x => x._2).reverse.take(maxSize).zipWithIndex.map(x => (x._2 + 1, x._1._1)).toDF("Rank","NPI_KEY_ID")
val CD01_top5 = outActUri_CD01_arr.take(maxSize).zipWithIndex.map(x => (x._2 + 1, x._1._1)).toDF("Rank","OAMTYPCD01")
val CD02_top5 = outActUri_CD02_arr.take(maxSize).zipWithIndex.map(x => (x._2 + 1, x._1._1)).toDF("Rank","OAMTYPCD02")
val CD03_avg = outActUri_CD03_arr.zipWithIndex.map(x => (x._2 + 1, x._1._2)).toDF("Rank", "OAMTYPCD03_AVG")
val CD04_avg = outActUri_CD04_arr.zipWithIndex.map(x => (x._2 + 1, x._1._2)).toDF("Rank", "OAMTYPCD04_AVG")
val CD05_avg = outActUri_CD05_arr.zipWithIndex.map(x => (x._2 + 1, x._1._2)).toDF("Rank", "OAMTYPCD05_AVG")

//최종적으로 join해서 합치기
val rankList = Seq(NCR_std_Info_top5, CD01_top5, CD02_top5, CD03_avg, CD04_avg, CD05_avg)

var Result_All = PassUri_top5
rankList.foreach{ DF =>
  Result_All = Result_All.join(DF, Seq("Rank"), "outer")
}
Result_All.sort("Rank").show

setMongoDF_result(spark, Result_All)
