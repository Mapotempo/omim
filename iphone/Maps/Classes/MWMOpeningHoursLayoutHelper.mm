#import "MWMOpeningHoursLayoutHelper.h"
#import "Common.h"
#import "MWMOpeningHours.h"
#import "MWMPlacePageData.h"
#import "MWMTableViewCell.h"
#import "UIColor+MapsMeColor.h"
#import "UIFont+MapsMeFonts.h"

#include "std/array.hpp"

namespace
{
array<NSString *, 2> const kCells = {{@"_MWMOHHeaderCell", @"_MWMOHSubCell"}};

NSAttributedString * richStringFromDay(osmoh::Day const & day, BOOL isClosedNow)
{
  auto const richString = ^NSMutableAttributedString * (NSString * str, UIFont * font, UIColor * color)
  {
    return [[NSMutableAttributedString alloc] initWithString:str
                                                  attributes:@{NSFontAttributeName : font,
                                                               NSForegroundColorAttributeName : color}];
  };

  auto str = richString(day.TodayTime(), [UIFont regular17], day.m_isOpen ? [UIColor blackPrimaryText] :
                        [UIColor red]);
  if (day.m_isOpen)
  {
    auto lineBreak = [[NSAttributedString alloc] initWithString:@"\n"];

    if (day.m_breaks.length)
    {
      [str appendAttributedString:lineBreak];
      [str appendAttributedString:richString(day.m_breaks, [UIFont regular13], [UIColor blackSecondaryText])];
    }

    if (isClosedNow)
    {
      [str appendAttributedString:lineBreak];
      [str appendAttributedString:richString(L(@"closed_now"), [UIFont regular13], [UIColor red])];
    }

    auto paragraphStyle = [[NSMutableParagraphStyle alloc] init];
    paragraphStyle.lineSpacing = 4;

    [str addAttributes:@{NSParagraphStyleAttributeName : paragraphStyle} range:{0, str.length}];
  }
  return str;
}

}  // namespace

@interface MWMPlacePageData()

- (vector<place_page::MetainfoRows> &)mutableMetainfoRows;

@end

#pragma mark - _MWMOHHeaderCell

@interface _MWMOHHeaderCell : MWMTableViewCell

@property(weak, nonatomic) IBOutlet UILabel * text;
@property(weak, nonatomic) IBOutlet UIImageView * arrowIcon;

@property(copy, nonatomic) TMWMVoidBlock tapAction;

@end

@implementation _MWMOHHeaderCell

- (IBAction)extendTap
{
  if (!self.tapAction)
    return;

  self.tapAction();
  [UIView animateWithDuration:kDefaultAnimationDuration
                   animations:^{
                     self.arrowIcon.transform =
                     CGAffineTransformIsIdentity(self.arrowIcon.transform)
                     ? CGAffineTransformMakeRotation(M_PI)
                     : CGAffineTransformIdentity;
                   }];
}

@end

#pragma mark - _MWMOHSubCell

@interface _MWMOHSubCell : MWMTableViewCell

@property(weak, nonatomic) IBOutlet UILabel * days;
@property(weak, nonatomic) IBOutlet UILabel * schedule;
@property(weak, nonatomic) IBOutlet UILabel * breaks;

@end

@implementation _MWMOHSubCell

@end


@interface MWMOpeningHoursLayoutHelper()
{
  vector<osmoh::Day> m_days;
}

@property(weak, nonatomic) UITableView * tableView;
@property(weak, nonatomic) MWMPlacePageData * data;

@property(copy, nonatomic) NSString * rawString;
@property(nonatomic) BOOL isClosed;
@property(nonatomic) BOOL isExtended;

@end

@implementation MWMOpeningHoursLayoutHelper

- (instancetype)initWithTableView:(UITableView *)tableView
{
  self = [super init];
  if (self)
  {
    _tableView = tableView;
    [self registerCells];
  }
  return self;
}

- (void)registerCells
{
  for (auto name : kCells)
    [self.tableView registerNib:[UINib nibWithNibName:name bundle:nil] forCellReuseIdentifier:name];
}

- (void)configWithData:(MWMPlacePageData *)data
{
  self.data = data;
  self.rawString = [data stringForRow:place_page::MetainfoRows::OpeningHours];
  self.isClosed = data.schedule == place_page::OpeningHours::Closed;
  m_days = [MWMOpeningHours processRawString:self.rawString];
}

- (UITableViewCell *)cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
  auto tableView = self.tableView;
  auto const & day = m_days[indexPath.row];

  if (self.data.metainfoRows[indexPath.row] == place_page::MetainfoRows::OpeningHours)
  {
    _MWMOHHeaderCell * cell = [tableView dequeueReusableCellWithIdentifier:[_MWMOHHeaderCell className]];

    if (m_days.size() > 1)
    {
      cell.tapAction = ^{
        self.isExtended = !self.isExtended;

        NSMutableArray<NSIndexPath *> * ip = [@[] mutableCopy];

        for (auto i = 1; i < self->m_days.size(); i++)
          [ip addObject:[NSIndexPath indexPathForRow:i inSection:1]];

        if (self.isExtended)
        {
          [self extendMetainfoRowsWithSize:ip.count];
          [tableView insertRowsAtIndexPaths:ip
                           withRowAnimation:UITableViewRowAnimationLeft];
        }
        else
        {
          [self reduceMetainfoRows];
          [tableView deleteRowsAtIndexPaths:ip
                           withRowAnimation:UITableViewRowAnimationLeft];
        }
      };
      cell.arrowIcon.hidden = NO;
    }
    else
    {
      cell.tapAction = nil;
      cell.arrowIcon.hidden = YES;
    }

    // This means that we couldn't parse opening hours string.
    if (m_days.empty())
      cell.text.text = self.rawString;
    else
      cell.text.attributedText = richStringFromDay(day, self.isClosed);

    return cell;
  }
  else
  {
    _MWMOHSubCell * cell = [tableView dequeueReusableCellWithIdentifier:[_MWMOHSubCell className]];
    cell.days.text = day.m_workingDays;
    cell.schedule.text = day.m_workingTimes ? day.m_workingTimes : L(@"closed");
    cell.breaks.text = day.m_breaks;
    return cell;
  }
}

- (void)extendMetainfoRowsWithSize:(NSUInteger)size
{
  if (size == 0)
  {
    NSAssert(false, @"Incorrect number of days!");
    return;
  }

  auto & metainfoRows = self.data.mutableMetainfoRows;
  using place_page::MetainfoRows;

  auto it = find(metainfoRows.begin(), metainfoRows.end(), MetainfoRows::OpeningHours);
  if (it == metainfoRows.end())
  {
    LOG(LERROR, ("Incorrect state!"));
    return;
  }

  metainfoRows.insert(++it, size, MetainfoRows::ExtendedOpeningHours);
}

- (void)reduceMetainfoRows
{
  auto & metainfoRows = self.data.mutableMetainfoRows;
  metainfoRows.erase(remove(metainfoRows.begin(), metainfoRows.end(), place_page::MetainfoRows::ExtendedOpeningHours), metainfoRows.end());
}

@end
