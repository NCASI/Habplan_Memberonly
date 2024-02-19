#' Plot output from multiple saved Habplan flow files
#'
#' Creates a figure which compares the flow outputs across time periods for multiple flow files
#' @param flow.data.1 Flow file saved by Habplan
#' @param flow.data.2 Flow file saved by Habplan
#' @param flow.data.3 Flow file saved by Habplan
#' @param nyear Number of years or time periods interested in (have data for)
#' @return A figure comparing multiple flows across time period
#' @examples
#' #Read in the example flows:
#' flow1 <- read.csv("./saveFlow1", sep="")
#' flow2 <- read.csv("./saveFlow2", sep="")
#' flow3 <- read.csv("./saveFlow3", sep="")
#'
#' #Run function with new flows
#' comPlot(flow.data.1 = flow1, flow.data.2 = flow2,
#'         flow.data.3 = flow3, nyear = 35)
#'
#' @export

comPlot <- function(flow.data.1, flow.data.2, flow.data.3,
                    nyear){

  flow.list.1 <- vector(mode = "list", length = nyear)

  #First flow here
  flow1 <- flow.data.1

  for (i in 1:length(flow.list.1)) {
    std <- cbind(flow1[2], flow1[i+3], flow1[i+(nyear+3)])
    colnames(std) <- c("id", "year", "flow")
    std$flow <- gsub('[,]', '', std$flow)
    std$year <- gsub('[,]', '', std$year)
    std$id <- gsub('[,]', '', std$id)
    std$flow <- as.numeric(std$flow)
    tot.flow <- sum(std$flow)
    flow.list.1[i] <- tot.flow
  }

  year <- c(1:nyear)
  flow <- t(flow.list.1)
  flow <- t(flow)
  f1.output <- cbind(year, flow)
  f1.output <- as.data.frame(f1.output)
  colnames(f1.output) <- c("year", "flow")
  f1.output$flow <- as.numeric(f1.output$flow)
  f1.output$year <- as.numeric(f1.output$year)
  f1.output$num <- "flow 1"

  #Second flow here
  flow.list.2 <- vector(mode = "list", length = nyear)
  flow1 <- flow.data.2

  for (i in 1:length(flow.list.2)) {
    std <- cbind(flow1[2], flow1[i+3], flow1[i+(nyear+3)])
    colnames(std) <- c("id", "year", "flow")
    std$flow <- gsub('[,]', '', std$flow)
    std$year <- gsub('[,]', '', std$year)
    std$id <- gsub('[,]', '', std$id)
    std$flow <- as.numeric(std$flow)
    tot.flow <- sum(std$flow)
    flow.list.2[i] <- tot.flow
  }

  year <- c(1:nyear)
  flow <- t(flow.list.2)
  flow <- t(flow)
  f2.output <- cbind(year, flow)
  f2.output <- as.data.frame(f2.output)
  colnames(f2.output) <- c("year", "flow")
  f2.output$flow <- as.numeric(f2.output$flow)
  f2.output$year <- as.numeric(f2.output$year)
  f2.output$num <- "flow 2"

  #Third flow here
  flow.list.3 <- vector(mode = "list", length = nyear)
  flow1 <- flow.data.3

  for (i in 1:length(flow.list.3)) {
    std <- cbind(flow1[2], flow1[i+3], flow1[i+(nyear+3)])
    colnames(std) <- c("id", "year", "flow")
    std$flow <- gsub('[,]', '', std$flow)
    std$year <- gsub('[,]', '', std$year)
    std$id <- gsub('[,]', '', std$id)
    std$flow <- as.numeric(std$flow)
    tot.flow <- sum(std$flow)
    flow.list.3[i] <- tot.flow
  }

  year <- c(1:nyear)
  flow <- t(flow.list.3)
  flow <- t(flow)
  f3.output <- cbind(year, flow)
  f3.output <- as.data.frame(f3.output)
  colnames(f3.output) <- c("year", "flow")
  f3.output$flow <- as.numeric(f3.output$flow)
  f3.output$year <- as.numeric(f3.output$year)
  f3.output$num <- "flow 3"

  all.output <- rbind(f1.output, f2.output, f3.output)

  #library(ggplot2)
  f1.plot <- ggplot(data = all.output) +
    geom_line(data = all.output, aes(x = year, y = flow, group = num, color = num),
              position = "identity") +
    geom_area(data = all.output, aes(x = year, y = flow, group = num, fill = num),
              alpha = 0.5, position = "identity") +
    #ylim(0, 10000) +
    #geom_hline(yintercept = target, linetype = 2, color = "red") +
    #geom_hline(yintercept = (target+th.hi), linetype = 2, color = "black") +
    #geom_hline(yintercept = (target-th.lo), linetype = 2, color = "black") +
    scale_color_viridis_d(option = "viridis", begin = 0.3, end = 0.6) +
    scale_fill_viridis_d(option = "viridis", begin = 0.3, end = 0.6) +
    scale_x_continuous(breaks=seq(0,nyear,5)) +
    ggtitle("Flow output over time") +
    xlab("Year") +
    ylab("Flow") +
    theme_classic() +
    guides(color = guide_legend(title = "Flow #"), fill = guide_legend(title = "Flow #"))
  #theme(legend.position = "none")

  ggsave(file = paste0("./Combined_flows.png"), width = 200, height = 120,
         dpi = 600, units = "mm")

  return(f1.plot)
}

