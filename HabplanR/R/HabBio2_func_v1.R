#' Convert data frames to Habplan-suitable flow files
#'
#' Creates flow files from csv input data
#' @param std.data Forest stand, regime, and outcome information
#' @param std.info Additional stand information
#' @param col The column number for the data interested in from std.data
#' @param nyear Number of years or time periods interested in (have data for)
#' @param HSI Threshold value of HSI for species of interest (0-0.99)
#' @return Saves a .dat file in the working directory ready for import to Habplan
#' @examples
#' #Read in stand and regime information
#' std.data <- read_csv("./fvs_results.csv")
#'
#' #Read in additional stand information - acreage, id, and description
#' std.info <- read_csv("./Stand_info.csv")
#'
#' #Look at the column headings to decide which column has flow results
#' colnames(new.data)
#'
#' #From the column names, we are interested in "HSI", which is
#' #column number 37. We can input this into our function.
#'
#' #Run function
#' rcw.flow <- habConvert(std.data = new.data, std.info = std.info, col = 37,
#'                        nyear = 35, HSI = 0.7)
#'
#' @export

HabBio2 <- function(std.data, std.info){

  #We need to extract year, stand id and flow

  #Get each stand name to run through loop
  std.id <- unique(std.data$StandID)

  #Create list to store stand data
  std.list <- vector(mode = "list", length = length(std.id))

  for (j in 1:length(std.list)) {
    #j<-2
    temp.data <- std.data %>%
      filter(StandID == std.id[j])

    regim <- unique(temp.data$RegimeKey)
    #regim <- data.frame(regim)
    #regim$std_id <- std.id[j]
    #regim$value <- 1
    #biol2 <- data.frame(cbind(regim$std_id, regim$regim, regim$value))
    #colnames(biol2) <- c("std_id", "regime", "value")

    reg.list <- vector(mode = "list", length = length(regim))
    for (o in 1:length(reg.list)) {
      #data <- paste0(std.id[j], ' ', regim[o], ' ', sample(1000, 1))
      data <- paste0(std.id[j], ' ', regim[o], ' ', 1)
      reg.list[[o]] <- data
    }
    bio <- unlist(reg.list)
    std.list[[j]] <- bio

  }
  final.data <- unlist(std.list)
  data.file <- file(paste0("./Biol2_test.dat"))
  writeLines(final.data, data.file)
  close(data.file)
  return(final.data)
}
