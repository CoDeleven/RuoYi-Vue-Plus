# Tour bulk import template

`tours_import_template.xlsx` — one Excel file, one plain sheet per thing,
linked by `tour_code`. No special encoding, no delimiters — just fill in rows
like a normal spreadsheet.

## Sheets

| Sheet | One row = | Maps to |
|---|---|---|
| **Instructions** | — | (read-me tab, not imported) |
| **Lists** | — | (dropdown reference data, not imported) |
| **Tours** | one tour | `holidays_tour` (+ `holidays_tour_tag_rel` via `collection_tag`) |
| **Itinerary** | one day of the tour | `holidays_tour_itinerary` |
| **ItineraryActivities** | one sub-bullet within a day (optional) | `holidays_tour_itinerary_activity` |
| **ServiceItems** | one inclusion/exclusion line | `holidays_tour_service_item` |
| **Departures** | one bookable scheduled trip date | `holidays_departure` |
| **Reviews** | one seeded review (optional) | `holidays_review` |

`tour_code` is the join key across every sheet — it must match exactly for
all rows belonging to the same tour.

Every constrained column (country, status, travel style, currency, Y/N, etc.)
has a real Excel dropdown — click the cell and a small arrow appears on the
right; picking from it guarantees the value matches exactly, no typos. All of
them pull from the **Lists** sheet — reference only, not read by the importer.

There is intentionally no "Destinations" or "Cities" sheet: this database has
no `holidays_tour_destination` table, and checking every existing tour showed
they all link their itinerary to a **country only** — never a specific city,
even for itineraries that visit several different cities within one country.
So the **Itinerary** sheet's `from_country`/`to_country` columns (plain
country names, picked from the **Lists** sheet) are all that's needed; the
app works out which countries a tour visits by reading those columns, instead
of a separate destinations table nothing else reads from.

(An earlier version of this template also had `from_city`/`to_city` columns
with a whole cascading-dropdown system behind them, built and tested against
this team's real spreadsheet app (WPS Office, not Microsoft Excel, which is
the actual default handler for `.xlsx` files here even though genuine Office
is also installed). It was dropped once checking the real data showed
city-level linkage isn't used anywhere in this app — the extra precision and
complexity weren't buying anything.)

## Column notes

- **Tours**: `travel_style`, `service_level`, `physical_rating`, `trip_type`,
  `currency`, `collection_tag` are entered as plain words (`Classic`,
  `Standard`, `Easy`, `Small Group`, `USD`, `Top Seller`) matching the
  dashboard's dropdown labels — the importer resolves these against the
  corresponding dictionaries (`holidays_tour_travel_style`,
  `holidays_tour_service_level`, `holidays_tour_physical_rating`,
  `holidays_tour_trip_type`, `holidays_currency_unit`,
  `holidays_tour_collection`). `status` is `Draft`, `Published` or `Off-shelf`.
  `collection_tag` can be left blank.
- **Itinerary**: `breakfast` / `lunch` / `dinner` are `Y` or `N` (stored as the
  `dining` dict's codes internally: 1=Breakfast, 2=Dinner, 3=Lunch — the
  importer handles that encoding, just use Y/N in the sheet). `from_country`/
  `to_country` say which country that day is in — leave both blank on days
  that don't cross into a new country (very common in the real data; only
  the day a border is crossed, or day 1's start, typically has this filled in).
- **ItineraryActivities** (optional): `day_number` must match a day already
  listed for the same `tour_code` on the Itinerary sheet — the importer links
  each activity to that day. `subtitle` is one or more short tags separated
  by commas (e.g. `classic, recommand`) — free text, can be left blank.
  `show_in_preview` is `Y` or `N`.
- **ServiceItems**: `type` is `Included` or `Excluded`.
- **Departures**: dates are `YYYY-MM-DD`. `departure_type` is `Fixed` or
  `Flexible` (leave blank for `Fixed`). `status` is `Open` (bookable), `Full`,
  `Ended` or `Cancelled` — matching the dashboard's own values (leave blank
  for `Open`). There's no `discount_rate` column: like the dashboard's own
  form, it's always computed automatically as `sale_price / base_price`,
  never entered by hand. `booked_count`/`available_count` aren't entered
  either — they start at `0` booked / fully available, same as creating a
  departure by hand.
- **Reviews** (optional): for seeding a new tour with initial reviews, not
  real customer feedback (which comes in through actual bookings and isn't
  part of this template). `rating` is `1`-`5`. `featured` is `Y`/`N`. These
  are always inserted as manager-entered and published, same as the
  dashboard's own "add review" defaults — there's no draft/pending option
  in the template.

## Not covered by this template

- **Cover image / map image** — added manually on the dashboard after import.
- **Activity icon** (`activity_icon` on `holidays_tour_itinerary_activity`) isn't
  in the template — it's picked from the dashboard's icon set, not something
  with a matching plain-text value to type in a spreadsheet. Set it manually
  on the dashboard after import if needed.

## Status

Backend + dashboard "Import" button are implemented — see the Tour list page
in the admin dashboard (线路管理 → 批量导入). Uploading this file creates the
tours, their itinerary, activities, inclusions/exclusions, departure dates
and reviews in one go. Each tour row is imported in its own transaction, so
a mistake on one tour (e.g. an unrecognized country name, or a `tour_code`
that already exists) doesn't stop the rest of the file from importing — the
result message lists exactly which rows failed and why.
