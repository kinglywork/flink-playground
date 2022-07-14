package com.playground.stock.model

final case class TransactionSummaryAndFinancialNews(
                                                     industry: String,
                                                     maybeTransactionSummary: Option[TransactionSummary],
                                                     maybeFinancialNews: Option[FinancialNews],
                                                   )

object TransactionSummaryAndFinancialNews {
  def fromTransactionSummary(transactionSummary: TransactionSummary): TransactionSummaryAndFinancialNews =
    TransactionSummaryAndFinancialNews(
      industry = transactionSummary.industry,
      maybeTransactionSummary = Some(transactionSummary),
      maybeFinancialNews = None,
    )

  def fromFinancialNews(financialNews: FinancialNews): TransactionSummaryAndFinancialNews =
    TransactionSummaryAndFinancialNews(
      industry = financialNews.industry,
      maybeTransactionSummary = None,
      maybeFinancialNews = Some(financialNews),
    )

  def aggregate(first: TransactionSummaryAndFinancialNews, second: TransactionSummaryAndFinancialNews): TransactionSummaryAndFinancialNews =
    TransactionSummaryAndFinancialNews(
      industry = first.industry,
      maybeTransactionSummary = second.maybeTransactionSummary.orElse(first.maybeTransactionSummary),
      maybeFinancialNews = second.maybeFinancialNews.orElse(first.maybeFinancialNews),
    )
}
