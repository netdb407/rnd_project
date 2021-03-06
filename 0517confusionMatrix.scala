import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils


// =============================원본 start===================================
// // Load training data in LIBSVM format
// val data = MLUtils.loadLibSVMFile(sc, "data/mllib/sample_binary_classification_data.txt")
//
// // Split data into training (60%) and test (40%)
// val Array(training, test) = data.randomSplit(Array(0.6, 0.4), seed = 11L)
// training.cache()
//
// // Run training algorithm to build the model
// val model = new LogisticRegressionWithLBFGS()
//   .setNumClasses(2)
//   .run(training)
//
// // Clear the prediction threshold so the model will return probabilities
// model.clearThreshold
//
// // Compute raw scores on the test set
// val predictionAndLabels = test.map { case LabeledPoint(label, features) =>
//   val prediction = model.predict(features)
//   (prediction, label)
// }
//
// // Instantiate metrics object
// val metrics = new BinaryClassificationMetrics(predictionAndLabels)
//
// // Precision by threshold
// val precision = metrics.precisionByThreshold
// precision.foreach { case (t, p) =>
//   println(s"Threshold: $t, Precision: $p")
// }
//
// // Recall by threshold
// val recall = metrics.recallByThreshold
// recall.foreach { case (t, r) =>
//   println(s"Threshold: $t, Recall: $r")
// }
//
// // Precision-Recall Curve
// val PRC = metrics.pr
//
// // F-measure
// val f1Score = metrics.fMeasureByThreshold
// f1Score.foreach { case (t, f) =>
//   println(s"Threshold: $t, F-score: $f, Beta = 1")
// }
//
// val beta = 0.5
// val fScore = metrics.fMeasureByThreshold(beta)
// f1Score.foreach { case (t, f) =>
//   println(s"Threshold: $t, F-score: $f, Beta = 0.5")
// }
//
// // AUPRC
// val auPRC = metrics.areaUnderPR
// println("Area under precision-recall curve = " + auPRC)
//
// // Compute thresholds used in ROC and PR curves
// val thresholds = precision.map(_._1)
//
// // ROC Curve
// val roc = metrics.roc
//
// // AUROC
// val auROC = metrics.areaUnderROC
// println("Area under ROC = " + auROC)

// =============================원본 end===================================




val List_origin = List("삼성", "네이버", "카카오", "다음", "구글", "하이닉스", "대우", "한화", "엔씨소프트", "현대")
val List_predict =  List("네이버", "삼성", "카카오", "다음", "하이닉스", "대우", "한화", "구글", "엔씨소프트", "현대")


// Load training data in LIBSVM format
//val data = MLUtils.loadLibSVMFile(sc, "data/mllib/sample_binary_classification_data.txt")

// Split data into training (60%) and test (40%)
//val Array(training, test) = data.randomSplit(Array(0.6, 0.4), seed = 11L)
val training = List("삼성", "네이버", "카카오", "다음", "구글", "하이닉스", "대우", "한화", "엔씨소프트", "현대")
val test = List("네이버", "삼성", "카카오", "다음", "하이닉스", "대우", "한화", "구글", "엔씨소프트", "현대")

// training.cache()

// Run training algorithm to build the model
val model = new LogisticRegressionWithLBFGS().setNumClasses(2).run(training)
//model이 우리꺼

// Clear the prediction threshold so the model will return probabilities
model.clearThreshold

// Compute raw scores on the test set
val predictionAndLabels = test.map { case LabeledPoint(label, features) =>
  val prediction = model.predict(features)
  (prediction, label)
}

// Instantiate metrics object
val metrics = new BinaryClassificationMetrics(predictionAndLabels)

// Precision by threshold
val precision = metrics.precisionByThreshold
precision.foreach { case (t, p) =>
  println(s"Threshold: $t, Precision: $p")
}

// Recall by threshold
val recall = metrics.recallByThreshold
recall.foreach { case (t, r) =>
  println(s"Threshold: $t, Recall: $r")
}

// F-measure
val f1Score = metrics.fMeasureByThreshold
f1Score.foreach { case (t, f) =>
  println(s"Threshold: $t, F-score: $f, Beta = 1")
}








private[ml] def evaluateClassificationModel(
      model: Transformer,
      data: DataFrame,
      labelColName: String): Unit = {
    val fullPredictions = model.transform(data).cache()
    val predictions = fullPredictions.select("prediction").rdd.map(_.getDouble(0))
    val labels = fullPredictions.select(labelColName).rdd.map(_.getDouble(0))
    // Print number of classes for reference.
    val numClasses = MetadataUtils.getNumClasses(fullPredictions.schema(labelColName)) match {
      case Some(n) => n
      case None => throw new RuntimeException(
        "Unknown failure when indexing labels for classification.")
    }
    val accuracy = new MulticlassMetrics(predictions.zip(labels)).accuracy
    println(s"  Accuracy ($numClasses classes): $accuracy")
  }









  import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS
  import org.apache.spark.mllib.evaluation.MulticlassMetrics
  import org.apache.spark.mllib.regression.LabeledPoint
  import org.apache.spark.mllib.util.MLUtils

  // Load training data in LIBSVM format
  val data = MLUtils.loadLibSVMFile(sc, "data/mllib/sample_multiclass_classification_data.txt")

  // Split data into training (60%) and test (40%)
  val Array(training, test) = data.randomSplit(Array(0.6, 0.4), seed = 11L)
  training.cache()

  // Run training algorithm to build the model
  val model = new LogisticRegressionWithLBFGS()
    .setNumClasses(3)
    .run(training)

  // Compute raw scores on the test set
  val predictionAndLabels = test.map { case LabeledPoint(label, features) =>
    val prediction = model.predict(features)
    (prediction, label)
  }

  // Instantiate metrics object
  val metrics = new MulticlassMetrics(predictionAndLabels)

  // Confusion matrix
  println("Confusion matrix:")
  println(metrics.confusionMatrix)

  // Overall Statistics
  val accuracy = metrics.accuracy
  println("Summary Statistics")
  println(s"Accuracy = $accuracy")

  // Precision by label
  val labels = metrics.labels
  labels.foreach { l =>
    println(s"Precision($l) = " + metrics.precision(l))
  }

  // Recall by label
  labels.foreach { l =>
    println(s"Recall($l) = " + metrics.recall(l))
  }

  // False positive rate by label
  labels.foreach { l =>
    println(s"FPR($l) = " + metrics.falsePositiveRate(l))
  }

  // F-measure by label
  labels.foreach { l =>
    println(s"F1-Score($l) = " + metrics.fMeasure(l))
  }

  // Weighted stats
  println(s"Weighted precision: ${metrics.weightedPrecision}")
  println(s"Weighted recall: ${metrics.weightedRecall}")
  println(s"Weighted F1 score: ${metrics.weightedFMeasure}")
  println(s"Weighted false positive rate: ${metrics.weightedFalsePositiveRate}")



//=======================================================================
//=======================================================================

1. 정답셋 글자로?정렬??
2. 결과셋 정렬
3. 인덱스로 비교?
//
// for(i<-0 until valueBystdNo_from_Map.size){
//   orderedIdx_byStd = sbjtCD_in_departNM_List.indexOf(valueBystdNo_from_Map(i).sbjtCD)::orderedIdx_byStd
//   //sbjtCD_in_departNM_List.indexOf(valueBystdNo_from_Map(i).sbjtCD) ??
//   //indexOf로 인덱스 가져오기
// }
//
// for(i<-0 until not_rated.size){
//   not_orderedIdx_byStd = sbjtCD_in_departNM_List.indexOf(not_rated(i).toString)::not_orderedIdx_byStd
// }
//
// orderedIdx_byStd = orderedIdx_byStd.sorted
//
// for(i<-0 until not_orderedIdx_byStd.size){
//   star_point_List = star_point_List.updated(not_orderedIdx_byStd(i), -1)
// }



//precision = List_recommend에서 일치하는 값 개수 / List_origin의 length
//recall = List_recommend에서 일치하는 값 개수 / List_recommend의 length
//F-measure = 2*((R*P)/(R+P))










//학번으로 학생의 학과 찾기 (dataframe) => 컴퓨터공학과 학생
//var departNM = clPassUri_DF.filter(clPassUri_DF("STD_NO").equalTo(s"${stdNO}"))
var departNM = "컴퓨터공학과"

//====================교과======================
val clPassUri = "V_STD_CDP_PASSCURI" //교과목 수료(class pass)
var clPassUri_DF = clPassUri_table.select(col("STD_NO"), col("SUST_CD_NM"), col("SBJT_KOR_NM"), col("SBJT_KEY_CD")).distinct.toDF

var sbjt_origin_temp1 = clPassUri_DF.filter(clPassUri_DF("SUST_CD_NM").equalTo(s"${departNM})"))
var sbjt_origin_temp2 = sbjt_origin_temp1.groupBy("SBJT_KEY_CD").count().orderBy($"count".desc)
val sbjt_origin_temp3 = sbjt_origin_temp2.select("code").limit(10)
val sbjt_origin_list_10 = sbjt_origin_temp3.rdd.map(r=>r(0)).collect.toList



val list_origin = List("교과1", "교과2", "카카오", "다음", "구글", "하이닉스", "대우", "한화", "엔씨소프트", "현대")
val list_recommend =  List("LG", "삼성", "카카오", "다음", "하이닉스")


var contain_count = 0
list_recommend.foreach{ list =>
  if(list_origin.contains(list)){
    contain_count = contain_count+1
  }
  contain_count
}

val precision = contain_count.toFloat/list_origin.length
val recall = contain_count.toFloat/list_recommend.length
val f_measure = 2*((recall*precision)/(recall+precision))


//====================비교과======================
//학과정보가 없어서 (학과-학번)만들고 학번으로 select해야하나 ;;ㅡㅡ
val ncrInfoUri = "CPS_NCR_PROGRAM_INFO"  //비교과 신청학생 정보에 학과정보 있어야해.ㅣ.;;; => 없어서 학과 학번 만들고 그거로 찾아야할듯..
var ncrInfoUri_DF = ncrInfoUri_table.select(col("NPI_KEY_ID"))

var ncr_origin_temp1 = ncrInfoUri_DF.filter(ncrInfoUri_DF("SUST_CD_NM").equalTo(s"${departNM})"))
var ncr_origin_temp2 = ncr_origin_temp1.groupBy("NPI_KEY_ID").count().orderBy($"count".desc)
val ncr_origin_temp3 = ncr_origin_temp2.select("code").limit(10)
val ncr_origin_list_10 = ncr_origin_temp3.rdd.map(r=>r(0)).collect.toList

//===================================================

컴공 학과 학번 먼저 만들고
그 학번이 수강한 ncr..만 select
그 학번이 비교과 안들었으면 지나치기


var clPassUri_DF_ncr = clPassUri_table.select(col("SUST_CD_NM"), col("STD_NO")).distinct.toDF

//map연산은 dataframe에 쓸 수 없기 때문에 list로 변환해야 하며 dataframe을 list로 변환하려면 df의 값 하나하나에 접근하기 위해 map 연산이 필요함
//광홍과df(clpass 교과목 수료 테이블에서 학과 별 학번 dataframe을 생성한 뒤 list로 변환)

// var departNM = "컴퓨터공학과"
var stdNO_in_departNM_ncr = clPassUri_DF_ncr.filter(clPassUri_DF_ncr("SUST_CD_NM").equalTo(s"${departNM}")).select(col("STD_NO")).distinct.rdd.map(r=>r(0)).collect.toList.map(_.toString)
// var stdNO_in_departNM_ncr = clPassUri_DF_ncr.filter(clPassUri_DF_ncr("SUST_CD_NM").equalTo(s"${departNM}")).select(col("STD_NO")).distinct.limit(10).rdd.map(r=>r(0)).collect.toList.map(_.toString)
