#' Plot output from a single saved Habplan flow file
#'
#' Creates a figure to visualize flow outputs across time periods for a single flow file
#' @param flow.data Flow file saved by Habplan
#' @param nyear Number of years or time periods interested in (have data for)
#' @return A figure comparing multiple flows across time period
#' @examples
#' #Read in the example flows:
#' flow1 <- read.csv("./saveFlow1", sep="")
#'
#' #Input the flow file into the function, and number of years
#' flowPlot(flow.data = flow1, nyear = 35)
#'
#' @export

flowPlot <- function(flow.data, nyear){
  #Set the length below as the number of years in model
  flow.list <- vector(mode = "list", length = nyear)

  flow1 <- flow.data

  for (i in 1:length(flow.list)) {
    std <- cbind(flow1[2], flow1[i+3], flow1[i+(nyear+3)])
    colnames(std) <- c("id", "year", "flow")
    std$flow <- gsub('[,]', '', std$flow)
    std$year <- gsub('[,]', '', std$year)
    std$id <- gsub('[,]', '', std$id)
    std$flow <- as.numeric(std$flow)
    tot.flow <- sum(std$flow)
    flow.list[i] <- tot.flow
  }

  year <- c(1:nyear)
  flow <- t(flow.list)
  flow <- t(flow)
  f1.output <- cbind(year, flow)
  f1.output <- as.data.frame(f1.output)
  colnames(f1.output) <- c("year", "flow")
  f1.output$flow <- as.numeric(f1.output$flow)
  f1.output$year <- as.numeric(f1.output$year)

  #library(ggplot2)
  f1.plot <- ggplot(data = f1.output) +
    geom_line(aes(x = year, y = flow)) +
    geom_area(aes(x = year, y = flow), fill = "grey", alpha = 0.7) +
    #geom_hline(yintercept = target, linetype = 2, color = "red") +
    #geom_hline(yintercept = (target+th.hi), linetype = 2, color = "black") +
    #geom_hline(yintercept = (target-th.lo), linetype = 2, color = "black") +
    #scale_color_viridis_d() +
    ggtitle("Flow output over time") +
    xlab("Year") +
    ylab("Flow") +
    theme_classic() +
    theme(legend.position = "none")

  ggsave(file = paste0("./example/RCW/RCW/Outputs/Single_flow.png"), width = 200, height = 120,
         dpi = 600, units = "mm")

  return(f1.plot)
}
